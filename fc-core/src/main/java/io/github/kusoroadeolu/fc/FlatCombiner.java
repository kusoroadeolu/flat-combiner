package io.github.kusoroadeolu.fc;


/*
 * Based on the paper https://people.csail.mit.edu/shanir/publications/Flat%20Combining%20SPAA%2010.pdf
 * The core idea of flat combining is the cost of obtaining a lock to a shared data structure
 * is amortized by threads publishing a request to a publication list and a combiner (a thread that acquired the lock)
 * is made aware of requests published by other waiting threads by scanning the publication list
 *
 * States:
 * A node can be said to be detached from the queue when its age is set to -1
 * A node can be said to be applied when its action is nulled
 * */

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/*
* Algo:
*  Load our node from the ThreadLocal field
*  Set node#action = action
*  If our node is inactive (age = -1),
*       Queue#enqueue();
*
*
*  Repeatedly: !lvIsApplied
*     If lvLock() == false && casLock(false, true)
*       If laAge == -1
*           Queue#enqueue();
*     Repeatedly:
*       int passes = 0;
*       If !atTail() && passes < COMBINE_PASS
*           scanCombineApply(); //Apply
*           ++combineCount;
*
*       If combineCount % COMBINE_PASS == 0
*           detachOldNodes();
*
*       lock#set(false); //Full volatile set here, to prevent reorderings past this, could use a set release but i feat previous writes could be reordered past the set release
*
*       int count = 0;
*       Repeatedly: ++count < SPIN_COUNT
*           If lvIsApplied return;
*
* */
public class FlatCombiner<T> {

    private final Lock lock;
    private final ThreadLocal<Node<T>> pNode;
    private final T item;
    private final MPSCQueue<T> pubList;
    private static final int MAX_COMBINE_PASS = 100;
    private int combinePass;

    public FlatCombiner(T item) {
        Objects.requireNonNull(item);
        this.lock = new ReentrantLock();
        this.pNode = ThreadLocal.withInitial(Node::new);
        this.item = item;
        this.pubList = new MPSCQueue<>();
    }

    public int combine(Consumer<T> action) {
        Objects.requireNonNull(action);
        Node<T> ours = pNode.get();
        Lock l = lock;
        MPSCQueue<T> list = pubList;
        ours.soAction(action); //A release fence in the case we're still active in the queue, if not then we ideally will be backed by the cas to head op
        int combineCount = -1;
        if (ours.laAge() == -1) {
            list.casToHead(ours);
        }

        //Volatile read
        while (!ours.isApplied()) {
            if (l.tryLock()) {
                try {
                    if (ours.laAge() == -1) list.casToHead(ours);
                    combineCount = 0;
                    Node<T> curr = list.lvHead();
                    ++combinePass;
                    while (curr != null && combineCount < MAX_COMBINE_PASS) {
                        var a =  curr.loAction();
                        if (a != null) {//Applied action skip
                            ++combineCount;
                            a.accept(item);
                            curr.spAge(combinePass); //Doesn't need to be a volatile write
                            curr.soAction(null); // mark as applied
                        }
                        curr = curr.laNext();
                    }


                    if (combinePass != 0 && combinePass % MAX_COMBINE_PASS == 0) {
                        list.detachOldNodes(combinePass, MAX_COMBINE_PASS);
                    }


                }finally {
                    lock.unlock();
                }

                return combineCount;
            }

            int count = 0;
            while (++count < 100) {
                if (ours.isApplied()) break;
            }
        }

        return combineCount;
    }



    @SuppressWarnings("unchecked")
    static class Node<T>  {
        volatile int age = -1;
        volatile Consumer<T> action;
        volatile Node<T> next;

        //Set plain, will be backed by a volatile cas
        void spNext(Node<T> node) {
            assert node != this;
            NEXT.set(this, node);
        }

        Node<T> lpNext(){
            return  (Node<T>) NEXT.get(this);
        }

        Node<T> laNext(){
           var next = (Node<T>) NEXT.getAcquire(this);
           assert next != this;
           return next;
        }

        boolean isApplied(){
           return ACTION.getAcquire(this) == null;
        }

        int laAge() {
           return (int) AGE.getAcquire(this);
        }

        void soAge(int a){
            AGE.setRelease(this, a);
        }

        void spAge(int a){
            AGE.set(this, a);
        }

        void soAction(Consumer<T> action){
            ACTION.setRelease(this, action);
        }

        Consumer<T> loAction(){
            return (Consumer<T>) ACTION.getAcquire(this);
        }
    }


    @SuppressWarnings("unchecked")
    static class MPSCQueue<T> {
        private volatile Node<T> head;

        public MPSCQueue() {
            this.head = new Node<>();
        }

        public void casToHead(Node<T> node){
            Node<T> next;
            do {
                next = lvHead();
                node.spNext(next); //Backed by volatile cas, if succeeds
                node.spAge(Integer.MAX_VALUE); //Dummy age to avoid getting pruned early, this will be reset by the combiner, plain write is backed by a volatile cas
            }while (!HEAD.compareAndSet(this, next, node));
        }

        //We want to prune old nodes, we have to avoid modifying the head, this should only be accessed by one thread at a time
        //Next will always be visible from the head, as long as, p != null, we keep pruning
        public void detachOldNodes(int count, int threshold) {
            Node<T> prev = lvHead(), curr = prev.lpNext();

            for (; curr != null; prev = lvHead(), curr = curr.lpNext()){
                Node<T> succ = curr.lpNext(); //Backed by volatile read
                if ((count - curr.age) > threshold) { //Can use a plain read for age here as well
                    prev.spNext(succ);
                    curr.soAge(-1); //Mark as detached
                } //No need to detach curr.next here
            }
        }


        private Node<T> lvHead(){
            return  (Node<T>) HEAD.getVolatile(this);
        }
    }


    private static final VarHandle ACTION;
    private static final VarHandle NEXT;
    private static final VarHandle HEAD;
    private static final VarHandle AGE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            ACTION = lookup.findVarHandle(Node.class, "action", Consumer.class);
            NEXT = lookup.findVarHandle(Node.class, "next", Node.class);
            HEAD = lookup.findVarHandle(MPSCQueue.class, "head", Node.class);
            AGE = lookup.findVarHandle(Node.class, "age", int.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
