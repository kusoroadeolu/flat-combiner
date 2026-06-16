package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import io.github.kusoroadeolu.fc.HandOffCombiner;
import io.github.kusoroadeolu.fc.WaitStrategy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)

/*
* Benchmark                         (type)   Mode  Cnt   Score   Error   Units
PriorityQueueBench.eightThreads   JDK  thrpt   45   7.738 ± 0.469  ops/us
PriorityQueueBench.fourThreads  JDK  thrpt   45   8.387 ± 0.493  ops/us
PriorityQueueBench.twoThreads  JDK  thrpt   45   7.586 ± 0.632  ops/us
*
*
* Benchmark                          (type)   Mode  Cnt   Score   Error   Units
PriorityQueueBench.eightThreads  Combiner  thrpt   30  11.063 ± 0.578  ops/us
PriorityQueueBench.eightThreads   Handoff  thrpt   30  10.407 ± 0.447  ops/us
PriorityQueueBench.fourThreads   Combiner  thrpt   30  11.612 ± 0.520  ops/us
PriorityQueueBench.fourThreads    Handoff  thrpt   30  10.742 ± 0.343  ops/us
PriorityQueueBench.twoThreads    Combiner  thrpt   30  10.569 ± 0.753  ops/us
PriorityQueueBench.twoThreads     Handoff  thrpt   30  10.817 ± 0.941  ops/us
* */

public class PriorityQueueBench {
    private Queue<Integer> queue;

    @Param({"JDK", "Combiner", "Handoff"})
    private String type;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean insert = true;
    }


    @Setup
    public void setup() {
        Queue<Integer> pq = new PriorityQueue<>();
        queue = switch (type){
            case "JDK" -> new ConcurrentLinkedQueue<>();
            case "Combiner" -> Combiners.queue(new FlatCombiner<>(pq), WaitStrategy.park(1));
            case "Handoff" -> Combiners.queue(new HandOffCombiner<>(pq), WaitStrategy.park(1));
            default -> throw new RuntimeException();
        };
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


    private void doWork(Blackhole bh, ThreadState ts) {
        boolean isInsert = ts.insert;
        ts.insert = !isInsert;
        bh.consume(isInsert
            ? queue.offer(ThreadLocalRandom.current().nextInt(10_000))
            : queue.poll());
    }
}