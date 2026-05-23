package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiner;
import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import io.github.kusoroadeolu.fc.WaitStrategy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)

//Bench for different wait strategies and their perf on thrpt


/*
Benchmark                           (strat)   Mode  Cnt   Score   Error   Units
WaitStrategyBench.eightThreads         spin  thrpt   30  11.952 ± 0.295  ops/us
WaitStrategyBench.eightThreads        yield  thrpt   30   8.101 ± 0.329  ops/us
WaitStrategyBench.eightThreads         park  thrpt   30  25.489 ± 1.068  ops/us
WaitStrategyBench.fourThreads          spin  thrpt   30  10.407 ± 0.794  ops/us
WaitStrategyBench.fourThreads         yield  thrpt   30  10.787 ± 0.672  ops/us
WaitStrategyBench.fourThreads          park  thrpt   30  27.009 ± 1.028  ops/us
WaitStrategyBench.twoThreads           spin  thrpt   30  10.931 ± 1.269  ops/us
WaitStrategyBench.twoThreads          yield  thrpt   30  15.552 ± 0.855  ops/us
WaitStrategyBench.twoThreads           park  thrpt   30  25.905 ± 0.601  ops/us
* //My guess here is modern cpus are very fast so spin waiting/yielding for a result in a loop is basically nothing,
*  a thread can blast through a spin wait loop in mere pico-seconds, so before a result is ready a thread couldve gone through the spin wait read loop,
*  try to acquire the lock multiple times in a nano second. However parking for a nano second actually subdues this issue, the cost of context switching
*  and having the thread park a bit to wait for its result, guarantees a higher chance that its result has been applied by the combiner in its first spin wait loop
*   reducing the cost of burning CPU and increasing thrpt/latency
*
* Latency
* Benchmark                           (strat)  Mode  Cnt     Score      Error  Units
WaitStrategyBench.eightThreads         spin  avgt   30     0.780 ±    0.037  us/op
WaitStrategyBench.eightThreads        yield  avgt   30     1.326 ±    0.074  us/op
WaitStrategyBench.eightThreads         park  avgt   30     0.461 ±    0.027  us/op
WaitStrategyBench.fourThreads          spin  avgt   30     0.469 ±    0.033  us/op
WaitStrategyBench.fourThreads         yield  avgt   30     0.461 ±    0.027  us/op
WaitStrategyBench.fourThreads          park  avgt   30     0.242 ±    0.026  us/op
WaitStrategyBench.twoThreads           spin  avgt   30     0.205 ±    0.016  us/op
WaitStrategyBench.twoThreads          yield  avgt   30     0.178 ±    0.006  us/op
WaitStrategyBench.twoThreads           park  avgt   30     0.122 ±    0.013  us/op
* */
public class WaitStrategyBench {

    private Queue<Integer> queue;
    @Param({"spin", "yield", "park"})
    private String strat;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean enqueue = true;
    }

    @Setup
    public void setup() {
        Combiner<Queue<Integer>> combiner = new FlatCombiner<>(new ArrayDeque<>(), 20, 500);
        queue = switch (strat) {
           case "spin" -> Combiners.queue(combiner, WaitStrategy.spinWait());
           case "yield" -> Combiners.queue(combiner, WaitStrategy.yield());
           case "park" -> Combiners.queue(combiner, WaitStrategy.park(1));
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

    static class Runner {
        static void main() throws RunnerException {
            Options options = new OptionsBuilder()
                    .include(WaitStrategyBench.class.getSimpleName())
                    .addProfiler(JavaFlightRecorderProfiler.class, "dir=C:\\jfr-fc")
                    .build();
            new org.openjdk.jmh.runner.Runner(options).run();
        }
    }
}

/* Result when we start combining from our node
* Benchmark                           (strat)   Mode  Cnt   Score   Error   Units
WaitStrategyBench.eightThreads         spin  thrpt   30  12.009 ± 0.804  ops/us
WaitStrategyBench.eightThreads        yield  thrpt   30   8.934 ± 0.379  ops/us
WaitStrategyBench.eightThreads         park  thrpt   30  25.839 ± 1.694  ops/us
WaitStrategyBench.fourThreads          spin  thrpt   30  14.277 ± 1.437  ops/us
WaitStrategyBench.fourThreads         yield  thrpt   30  15.593 ± 0.966  ops/us
WaitStrategyBench.fourThreads          park  thrpt   30  26.810 ± 5.978  ops/us
WaitStrategyBench.sixteenThreads       spin  thrpt   30   7.034 ± 0.944  ops/us
WaitStrategyBench.sixteenThreads      yield  thrpt   30  10.995 ± 0.565  ops/us
WaitStrategyBench.sixteenThreads       park  thrpt   30  25.464 ± 1.661  ops/us
WaitStrategyBench.thirtyTwoThreads     spin  thrpt   30   4.273 ± 0.631  ops/us
WaitStrategyBench.thirtyTwoThreads    yield  thrpt   30  11.550 ± 0.338  ops/us
WaitStrategyBench.thirtyTwoThreads     park  thrpt   30  24.943 ± 1.309  ops/us
WaitStrategyBench.twoThreads           spin  thrpt   30  21.966 ± 1.889  ops/us
WaitStrategyBench.twoThreads          yield  thrpt   30  23.055 ± 0.936  ops/us
WaitStrategyBench.twoThreads           park  thrpt   30  22.197 ± 2.981  ops/us
* */