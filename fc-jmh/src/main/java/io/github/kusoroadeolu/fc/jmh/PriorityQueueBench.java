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
* Benchmark                                                            (type)   Mode  Cnt   Score   Error   Units
PriorityQueueBench.eightThreads                                         JDK  thrpt   45   7.738 ± 0.469  ops/us
PriorityQueueBench.eightThreads                                    Combiner  thrpt   45  11.236 ± 0.194  ops/us
PriorityQueueBench.fourThreads                                          JDK  thrpt   45   8.387 ± 0.493  ops/us
PriorityQueueBench.fourThreads                                     Combiner  thrpt   45  11.473 ± 0.300  ops/us
PriorityQueueBench.sixteenThreads                                       JDK  thrpt   45   7.534 ± 0.755  ops/us
PriorityQueueBench.sixteenThreads                                  Combiner  thrpt   45  10.284 ± 0.325  ops/us
PriorityQueueBench.thirtyTwoThreads                                     JDK  thrpt   45   7.136 ± 0.801  ops/us
PriorityQueueBench.thirtyTwoThreads                                Combiner  thrpt   45   9.665 ± 0.224  ops/us
PriorityQueueBench.twoThreads                                           JDK  thrpt   45   7.586 ± 0.632  ops/us
PriorityQueueBench.twoThreads                                      Combiner  thrpt   45  12.190 ± 0.228  ops/us
* */

public class PriorityQueueBench {
    private Queue<Integer> queue;

    //@Param({"JDK", "Combiner"})
    private String type;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean insert = true;
    }

    @Setup
    public void setup() {
        Queue<Integer> pq = new PriorityQueue<>();
        queue = /*type.equals("JDK") ? new PriorityBlockingQueue<>() :*/ Combiners.queue(new FlatCombiner<>(pq),  WaitStrategy.park(1));
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