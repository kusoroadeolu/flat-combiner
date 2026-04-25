package io.github.kusoroadeolu.fc.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

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
PriorityBlockingQueueBench.eightThreads      thrpt   45  7.028 ± 1.065  ops/us
PriorityBlockingQueueBench.fourThreads       thrpt   45  7.277 ± 0.994  ops/us
PriorityBlockingQueueBench.sixteenThreads    thrpt   45  6.761 ± 1.091  ops/us
PriorityBlockingQueueBench.thirtyTwoThreads  thrpt   45  6.471 ± 1.084  ops/us
PriorityBlockingQueueBench.twoThreads        thrpt   45  6.301 ± 0.790  ops/us
*
*
*
* Performs significantly better than both flat combiners even though the err margins are a bit high
* */

public class PriorityBlockingQueueBench {

    private PriorityBlockingQueue<Integer> queue;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean insert = true;
    }

    @Setup
    public void setup() {
        queue = new PriorityBlockingQueue<>();
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