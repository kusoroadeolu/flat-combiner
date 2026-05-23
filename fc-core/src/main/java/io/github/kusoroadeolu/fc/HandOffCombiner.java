package io.github.kusoroadeolu.fc;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.function.Function;


/*
* A combiner which uses node based handoff rather than threads trying to acquire a lock
*
* Each node has 3 fields
* An status (int) field indicating if a node is a combiner or not
* An action field indicating if a node has been applied or not (null == applied)
* A next node pointer indicating a next node
*
* High Level Algo
*  The combiner class starts of with a node with a COMBINER status
*  Each thread starts with a thread local node field with a null function, null next field and NOT_COMBINER status
*  We set our combiner status of our thread local node to NOT_COMBINER
*  We then atomically swap our node with the current tail, we publish our function (plain write). So our old node is the current tail
*  we then set our next field to our old node using a set release write
*  If the node we swapped with has a COMBINER status, we start combining from that node
*  We stop combining when n.next == null (indicating this is the current tail and combiner, or that a combiner hasn't set its next node yet)
*  We then use a set release write to set n as the current combiner
*
* If we're not the combiner, we keep spinning until status = COMBINER
* We then fall through, seeing our node has been applied (function == null) and return
*
*
*
* This combiner is pretty susceptible to cache misses (not false sharing) as a combiner loading a node that was previously in its thread local,
* can force threads to reread the cache line which that node resides
*
*
* Given a combiner with 3 initial threads T1, T2, T3, where A is the initial combiner tail and T1, T2 and T3 execute in their natural numeric order
*
* A -> T1 (new tail)
* A (T1) -> T1 (T2) -> T2 (new tail)
* A(T1) -> T1 (T2) -> T2 (T3) -> T3 (tail, will be set as combiner)
* A -> null , T1 -> null
* T2(Combiner) -> T3 (T1) -> A
* */

// Based on this GitHub gist: https://gist.github.com/duarten/62841b0c32181bfd7ee9b1e19bcd078e
@SuppressWarnings("unchecked")
public class HandOffCombiner<T> implements Combiner<T>{

    private volatile Node<T, Object> tail;
    private final T item;
    private final int maxCombinePass;
    private final ThreadLocal<Node<T, Object>> pNode;

    public HandOffCombiner(T item, int maxCombinePass) {
        this.item = item;
        this.maxCombinePass = maxCombinePass;
        this.pNode = ThreadLocal.withInitial(Node::new);
        this.tail = new Node<>(Node.COMBINER);
    }

    public HandOffCombiner(T item) {
        this(item, 20);
    }

    @Override
    public <R> R combine(Function<T, R> action) {
       return combine(action, WaitStrategy.spinWait());
    }

    @Override
    public <R> R combine(Function<T, R> action, WaitStrategy strategy) {
        Objects.requireNonNull(action);
        var old = (Node<T, R>) pNode.get();
        old.spNotCombiner();

        // Volatile write makes status visible to any other thread that takes that node
        Node<T, R> ours = (Node<T, R>) TAIL.getAndSet(this, old); //A brief moment where a combiner can't reach us, if we're not the combiner
        ours.action = action;
        ours.soNext(old); //Set old to our next, makes action immediately visible
        pNode.set((Node<T, Object>) ours);

        while (ours.loStatus() == Node.NOT_COMBINER) {
            strategy.idle();
        } //Acquire read from status should make function and value immediately visible

        if (ours.action == null) return ours.value;


        Node<T, R> curr = ours;
        Node<T, R> n;
        int mcp = maxCombinePass;
        for (int c = 0; c < mcp && (n = curr.loNext()) != null ; ++c, curr = n) {
            curr.value = curr.action.apply(item);
            curr.sopNext(null); //We can use a plain write here as a combiner can't read nodes above,
            // though to ensure the holding node thread sees the write and doesn't hold ref to a node which
            // is linked to the rest of the list. Under high contention, a plain write here might not matter.
            //Let's use an opaque write just to be safe that a thread doesn't hold a "next" ref to a node for too long
            //Though this is probably a non issue
            curr.soStatus(Node.COMBINER);
        }

        curr.soStatus(Node.COMBINER);
        return ours.value;
    }

    static class Node<T, R> {
        private static final int COMBINER = 1;
        private static final int NOT_COMBINER = 0;

        R value;
        Function<T, R> action;
        volatile int status = NOT_COMBINER;
        volatile Node<T, R> next;

        public Node(int status) {
            this.status = status;
        }

        void spNotCombiner() {
            STATUS.set(this, Node.NOT_COMBINER);
        }

        void soStatus(int status) {
            STATUS.setRelease(this, status);
        }

        void sopNext(Node<T, R> next) {
            NEXT.setOpaque(this, next);
        }

        Node<T, R> loNext() {
           return (Node<T, R>) NEXT.getAcquire(this);
        }

        void soNext(Node<T, R> next) {
            NEXT.setRelease(this, next);
        }

        int loStatus() {
            return (int) STATUS.getAcquire(this);
        }

        public Node() {}
    }


    private static final VarHandle STATUS;
    private static final VarHandle NEXT;
    private static final VarHandle TAIL;

    static {
        var l = MethodHandles.lookup();
        try {
            STATUS = l.findVarHandle(Node.class, "status", int.class);
            NEXT = l.findVarHandle(Node.class, "next", Node.class);
            TAIL = l.findVarHandle(HandOffCombiner.class, "tail", Node.class);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
