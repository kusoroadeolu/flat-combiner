package io.github.kusoroadeolu.stress;

import io.github.kusoroadeolu.fc.FlatCombiner;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CombinerStress {

    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Invariant maintained")
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "Invariant violated")
    @State
    public static class QueueAddRemoveTest {
        public final FlatCombiner<Queue<Integer>> fc;
        public final Queue<Integer> queue;

        public QueueAddRemoveTest() {
            this.queue = new ArrayDeque<>();
            this.fc = new FlatCombiner<>(queue);
            queue.add(1); queue.add(2); queue.add(3);

        }


        @Actor
        public void actor() {
            fc.combine(Queue::poll);
            fc.combine(Queue::poll);
        }


        @Arbiter
        public void arbiter(I_Result r) {
            List<Integer> ls = new ArrayList<>(1);
            fc.combine(q -> {
                int size = q.size();
                ls.addFirst(size);
            });

            r.r1 = ls.getFirst();
        }
    }

    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "Pruning succeeded")
    @State
    public static class DeadNodeStress {
        public final FlatCombiner<Integer> fc;

        public DeadNodeStress() {
            this.fc = new FlatCombiner<>(0);

        }

        @Actor
        public void actor1() {
            fc.combine(i -> ++i);
        }

        @Actor
        public void actor2() {
            fc.combine(i -> ++i);
        }

        @Actor
        public void actor3() {
            fc.combine(i -> ++i);
        }


        @Arbiter
        public void arbiter(I_Result r) {
             r.r1 = fc.deadNodeCount();
        }
    }

    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Can reach tail")
    @State
    public static class TailInvariantStress {
        public final FlatCombiner<Integer> fc;

        public TailInvariantStress() {
            this.fc = new FlatCombiner<>(0);

        }

        @Actor
        public void actor1() {
            fc.combine(i -> ++i);
        }

        @Actor
        public void actor2() {
            fc.combine(i -> ++i);
        }

        @Actor
        public void actor3() {
            fc.combine(i -> ++i);
        }


        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = fc.canReachTail() ? 1 : 0;
        }
    }
}
