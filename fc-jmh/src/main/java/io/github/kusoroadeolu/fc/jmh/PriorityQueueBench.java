package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import io.github.kusoroadeolu.fc.WaitStrategy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(3)

/*
* Benchmark                                     Mode  Cnt  Score   Error   Units
PriorityQueueBench.eightThreads      thrpt   45  7.028 ± 1.065  ops/us
PriorityQueueBench.fourThreads       thrpt   45  7.277 ± 0.994  ops/us
PriorityQueueBench.sixteenThreads    thrpt   45  6.761 ± 1.091  ops/us
PriorityQueueBench.thirtyTwoThreads  thrpt   45  6.471 ± 1.084  ops/us
PriorityQueueBench.twoThreads        thrpt   45  6.301 ± 0.790  ops/us
*
*
*
* */

public class PriorityQueueBench {
    private Queue<Integer> queue;

    @Param({"JDK", "Combiner"})
    private String type;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean insert = true;
    }

    @Setup
    public void setup() {
        Queue<Integer> pq = new PriorityQueue<>();
        queue = type.equals("JDK") ? new PriorityBlockingQueue<>() : Combiners.queue(new FlatCombiner<>(pq),  WaitStrategy.park(1));
        for (int i = 0; i < 1000; i++) queue.offer(i);
    }

    @Threads(2)
    @Benchmark
    public void twoThreads(Blackhole bh, ThreadState ts) {
        doWork(bh, ts);
    }

    @Threads(4)
    @Benchmark
    public void fourThreads(Blackhole bh, ThreadState ts) {
        doWork(bh, ts);
    }

    @Threads(8)
    @Benchmark
    public void eightThreads(Blackhole bh, ThreadState ts) {
        doWork(bh, ts);
    }


    @Threads(16)
    @Benchmark
    public void sixteenThreads(Blackhole bh, ThreadState ts) {
        doWork(bh, ts);
    }

    @Threads(32)
    @Benchmark
    public void thirtyTwoThreads(Blackhole bh, ThreadState ts) {
        doWork(bh, ts);
    }

    private void doWork(Blackhole bh, ThreadState ts) {
        boolean isInsert = ts.insert;
        ts.insert = !isInsert;
        bh.consume(isInsert
            ? queue.offer(ThreadLocalRandom.current().nextInt(10_000))
            : queue.poll());
    }
}