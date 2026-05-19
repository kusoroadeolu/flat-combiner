package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiner;
import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import io.github.kusoroadeolu.fc.WaitStrategy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/* Thrpt
* Benchmark                       (durationNs)   Mode  Cnt   Score   Error   Units
ParkDurationBench.eightThreads             1  thrpt   45  22.081 ± 0.876  ops/us
ParkDurationBench.eightThreads            10  thrpt   45  22.331 ± 1.095  ops/us
ParkDurationBench.eightThreads           100  thrpt   45  22.396 ± 1.176  ops/us
ParkDurationBench.fourThreads              1  thrpt   45  25.468 ± 0.697  ops/us
ParkDurationBench.fourThreads             10  thrpt   45  25.362 ± 0.951  ops/us
ParkDurationBench.fourThreads            100  thrpt   45  25.609 ± 0.718  ops/us
ParkDurationBench.twoThreads               1  thrpt   45  23.308 ± 0.823  ops/us
ParkDurationBench.twoThreads              10  thrpt   45  25.809 ± 0.394  ops/us
ParkDurationBench.twoThreads             100  thrpt   45  24.941 ± 0.327  ops/us
* */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)

public class ParkDurationBench {
    private Queue<Integer> queue;
    @Param({"1", "10", "100"})
    private String durationNs;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean enqueue = true;
    }

    @Setup
    public void setup() {
        Combiner<Queue<Integer>> combiner = new FlatCombiner<>(new ArrayDeque<>(), 20, 500);
        queue = switch (durationNs) {
            case "1" -> Combiners.queue(combiner, WaitStrategy.park(1));
            case "10" -> Combiners.queue(combiner, WaitStrategy.park(10));
            case "100" -> Combiners.queue(combiner, WaitStrategy.park(100));
            default -> throw new IllegalArgumentException();
        };

        for (int i = 0; i < 1000; i++) queue.offer(i);
    }

    @Threads(2)
    @Benchmark
    public void twoThreads(Blackhole bh, ThreadState ts) {
        addOrRemove(bh, ts);
    }

    @Threads(4)
    @Benchmark
    public void fourThreads(Blackhole bh, ThreadState ts) {
        addOrRemove(bh, ts);
    }

    @Threads(8)
    @Benchmark
    public void eightThreads(Blackhole bh, ThreadState ts) {
        addOrRemove(bh, ts);
    }

    void addOrRemove(Blackhole bh, ThreadState ts){
        boolean isEnqueue = ts.enqueue;
        ts.enqueue = !isEnqueue;
        if (isEnqueue) bh.consume(queue.offer(42));
        else bh.consume(queue.poll());
    }

}
