package io.github.kusoroadeolu.stress;

import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.Queue;

public class CombinerStress {

    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Invariant maintained")
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "Invariant violated")
    @State
    public static class QueueAddRemoveTest {
        public final Queue<Integer> queue;

        public QueueAddRemoveTest() {
            this.queue = Combiners.queue();
            queue.add(1); queue.add(2); queue.add(3);

        }


        @Actor
        public void actor() {
            queue.poll();
            queue.poll();
        }


        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = queue.size();
        }
    }

    @JCStressTest
    @Outcome(id = "1, 1, 1", expect = Expect.ACCEPTABLE, desc = "All visible")
    @State
    //Our result should never be null, though null can be a valid result irl, this is just a test to ensure our item is always visible
    public static class ResultVisibilityTest {
        public final FlatCombiner<Integer> fc;
        private static final int INVALID = -1;

        public ResultVisibilityTest() {
            this.fc = new FlatCombiner<>(1);
        }

        @Actor
        public void actor1(III_Result iii) {
            java.lang.Integer res = fc.combine(i -> i * 5);
            if (iii.r1 == INVALID) return; //Don't return
            if (res == null) iii.r1 = INVALID;
            else iii.r1 = 1;
        }

        @Actor
        public void actor2(III_Result iii) {
            java.lang.Integer res = fc.combine(i -> i * 5);
            if (iii.r2 == INVALID) return; //Don't return
            if (res == null) iii.r2 = INVALID;
            else iii.r2 = 1;
        }

        @Actor
        public void actor3(III_Result iii) {
            java.lang.Integer res = fc.combine(i -> i * 5);
            if (iii.r3 == INVALID) return; //Don't return
            if (res == null) iii.r3 = INVALID;
            else iii.r3 = 1;
        }


        @Arbiter
        public void arbiter(III_Result iii) {
            if (iii.r1 == INVALID || iii.r2 == INVALID || iii.r3 == INVALID) {
                iii.r1 = INVALID;
            } else {
                iii.r1 = 1;
            }

            iii.r2 = 1;
            iii.r3 = 1;
        }
    }

    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "No dead nodes")
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
