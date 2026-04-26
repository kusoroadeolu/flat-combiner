package io.github.kusoroadeolu.fc;


/*
 * Based on the paper https://people.csail.mit.edu/shanir/publications/Flat%20Combining%20SPAA%2010.pdf
 * The core idea of flat combining is the cost of obtaining a lock to a shared data structure
 * is amortized by threads publishing a request to a publication list and a combiner (a thread that acquired the lock)
 * is made aware of requests published by other waiting threads by scanning the publication list
 *
 * States:
 * A node can be said to be detached from the queue when its age is set to -1
 * A node's result can be said to be applied when its action is nulled.
 * Happens before - visibility of the result is guaranteed by a set release to a node's action, a plain write to the result is backed by this write.
 * A get acquire read is made before the result is read
 * */

import org.openjdk.jol.info.ClassLayout;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

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
public class FlatCombiner<T> implements Combiner<T>{
    private final ReentrantLock lock;
    private final ThreadLocal<Node<T, Object>> pNode;
    private final T item;
    private final PublicationQueue<T, Object> pubList;
    private final int maxCombinePass;
    private final int pruneThreshold;
    private int combinePass;

    public FlatCombiner(T item, int combinePass, int pruneThreshold) {
        this.lock = new ReentrantLock();
        this.pNode = ThreadLocal.withInitial(Node::new);
        this.item = Objects.requireNonNull(item);
        this.pubList = new PublicationQueue<>();
        this.maxCombinePass = Validator.ensureGreaterThanOrEqualToZero( combinePass, "Combine pass");
        this.pruneThreshold = Validator.ensureGreaterThanOrEqualToZero(pruneThreshold, "Prune threshold");
    }

    public FlatCombiner(T item) {
        this(item, 20, 100);
    }


    public <R>R combine(Function<T, R> action) {
        return combine(action, WaitStrategy.yield());
    }

    @SuppressWarnings("unchecked")
    public <R> R combine(Function<T, R> action, WaitStrategy strategy) {
        Objects.requireNonNull(action);
        Node<T, R> ours = (Node<T, R>) pNode.get();
        ReentrantLock l = lock;
        PublicationQueue<T, R> list = (PublicationQueue<T, R>) pubList;

        //Ideally the paper pushes towards a plain write for the action field, I do believe it is under the assumption this write will be backed by the cas to head op when enqueueing,
        // there's no HB visibility guarantee between this write being seen by the combiner if we use a plain write
        ours.spItem(null); //Ensure we don't write after we've published our action, could lead to a race condition where we overwrite if the combiner has already applied our action
        ours.soAction(action); //A release fence in the case we're still active in the queue. //Linearizability point where we are active if we're already on the queue


        if (ours.loAge() == -1) {
            list.casToHead(ours);
        }

        //Volatile read.
        while (!ours.isApplied()) { //Get acquire, item should always be visible if this is read
            if (l.tryLock()) {
                try {
                    if (ours.lpAge() == -1) list.casToHead(ours); //Use a plain read here due to get_acquire/set_release guarantees from acquiring the lock
                    int combineCount = 0;
                    Node<T, R> h = list.lvHead();
                    Node<T, R> curr = h;

                    int mcp = maxCombinePass;
                    int pt = pruneThreshold;
                    ++combinePass;
                    while (curr != null && combineCount < mcp) {
                        var a =  curr.loAction();
                        if (a != null) {
                            ++combineCount;
                            apply(curr, a);
                        }

                        curr = curr.loNext();
                    }

                    if (combinePass != 0 && combinePass % pt == 0) {
                        list.detachOldNodes(combinePass, pt);
                    }

                    if (!ours.isAppliedPlain())
                        apply(ours, ours.lpAction()); //Plain reads and writes here since we're writing to our own thread

                    return ours.lpItem(); //We need an acquire read here, but why?
                }finally {
                    l.unlock();
                }

            }

            int count = 0;
            while (++count < 100) {
                if (ours.isApplied()) return ours.lpItem();
                strategy.idle();
            }

            if (ours.loAge() == -1) list.casToHead(ours); //Re-append if we're dead
        }

        return ours.lpItem();
    }

    <R>void apply(Node<T, R> node, Function<T, R> a) {
        node.spItem(a.apply(item)); //Plain write to the node's result, always backed
        node.spAge(combinePass); //Doesn't need to be a volatile write since only the combiner ever reads it
        node.soAction(null);
    }


    //For stress tests
    public int deadNodeCount(){
        return pubList.countDeadNodes();
    }

    public boolean canReachTail(){
        return pubList.canReachTail();
    }

    @SuppressWarnings("unchecked")
    static class Node<T, R>{
        volatile Function<T, R> action; //We're spinning on this, need to ensure it is on its own cache line
        volatile int age = -1;
        volatile Node<T, R> next;
        R item;

        //Set plain, will be backed by a volatile cas
        public void spNext(Node<T, R> node) {
            NEXT.set(this, node);
        }

        Node<T, R> lpNext(){
            return (Node<T, R>) NEXT.get(this);
        }

        Node<T, R> loNext(){
           return  (Node<T, R>) NEXT.getAcquire(this);
        }

        boolean isApplied(){
           return loAction() == null;
        }

        boolean isAppliedPlain(){
            return ACTION.get(this) == null;
        }

        int loAge() {
           return (int) AGE.getAcquire(this);
        }

        int lpAge() {
            return (int) AGE.get(this);
        }

        void soAge(int a){
            AGE.setRelease(this, a);
        }

        public void spAge(int a){
            AGE.set(this, a);
        }

        public void spItem(R item){
            this.item = item;
        }

        public R lpItem(){
            return item;
        }

        void soAction(Function<T, R> action){
            ACTION.setRelease(this, action);
        }

        Function<T, R> loAction(){
            return (Function<T, R>) ACTION.getAcquire(this);
        }

        Function<T, R> lpAction(){
            return (Function<T, R>) ACTION.get(this);
        }

        @Override
        public String toString() {
            return "Node[" +
                    "action=" + action +
                    ", age=" + age +
                    ", next=" + next +
                    ", item=" + item +
                    ']';
        }
    }


    @SuppressWarnings("unchecked")
    static class PublicationQueue<T, R> {
        private volatile Node<T, R> head;

        public PublicationQueue() {
            this.head = null;
        }

        public void casToHead(Node<T, R> node){
            Node<T, R> next;
            do {
                next = lvHead();
                node.spNext(next); //Backed by volatile write, if it succeeds
                node.spAge(Integer.MAX_VALUE); //Dummy age to avoid getting re pruned immediately, this will be reset by the combiner.
                // Plain write is backed by a volatile cas

            }while (!HEAD.compareAndSet(this, next, node));

        }

        //We want to prune old nodes, we have to avoid modifying the head, this should only be accessed by one thread at a time
        //Next will always be visible from the head, as long as, p != null, we keep pruning
        public void detachOldNodes(int count, int threshold) {
            Node<T, R> prev = lvHead();
            Node<T, R> curr = prev.lpNext(), succ;

            for (; curr != null; curr = succ){
                succ = curr.loNext();
                if ((count - curr.loAge()) > threshold) {
                    prev.spNext(succ);
                    curr.soAge(-1);
                } else {
                    prev = curr; // only advance prev if curr survived
                }
            }
        }

        public Node<T, R> lvHead(){
            return (Node<T, R>) HEAD.getVolatile(this);
        }

        public int countDeadNodes(){
            var curr = lvHead();
            int i = 0, count = 0;
            for (; curr != null; curr = curr.loNext(), ++i){
                if (curr.loAge() == -1) ++count;
            }

            return count;
        }

        public boolean assertOursInQueue(Node<T, R> curr, Node<T, R> ours) {
            for (; curr != null; curr = curr.loNext()){
                if (curr == ours) return true;
            }
            return false;
        }

        public boolean canReachTail(){
            var curr = lvHead();
            int steps = 0;
            for (;  curr != null; curr = curr.loNext()) {
                if (++steps > 1000) return false;

            }
            return true;
        }
    }


    private static final VarHandle ACTION;
    private static final VarHandle NEXT;
    private static final VarHandle HEAD;
    private static final VarHandle AGE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            ACTION = lookup.findVarHandle(Node.class, "action", Function.class);
            NEXT = lookup.findVarHandle(Node.class, "next", Node.class);
            HEAD = lookup.findVarHandle(PublicationQueue.class, "head", Node.class);
            AGE = lookup.findVarHandle(Node.class, "age", int.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static class Main {
        static void main() {
            System.out.println(ClassLayout.parseClass(Node.class).toPrintable());
        }
    }
}
