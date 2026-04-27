package io.github.kusoroadeolu.fc.jmh;

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

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(3)

//Measures flat combining with a park wait strategy against the JDK lock free linear queue

/*
* Benchmark                                (type)   Mode  Cnt   Score   Error   Units
SequentialQueueBench.eightThreads           JDK  thrpt   45   9.952 ± 0.155  ops/us
SequentialQueueBench.eightThreads      Combiner  thrpt   45  18.323 ± 0.471  ops/us
SequentialQueueBench.fourThreads            JDK  thrpt   45   8.936 ± 0.149  ops/us
SequentialQueueBench.fourThreads       Combiner  thrpt   45  19.462 ± 0.451  ops/us
SequentialQueueBench.sixteenThreads         JDK  thrpt   45  10.234 ± 0.094  ops/us
SequentialQueueBench.sixteenThreads    Combiner  thrpt   45  18.456 ± 0.627  ops/us
SequentialQueueBench.thirtyTwoThreads       JDK  thrpt   45  10.337 ± 0.097  ops/us
SequentialQueueBench.thirtyTwoThreads  Combiner  thrpt   45  18.279 ± 0.630  ops/us
SequentialQueueBench.twoThreads             JDK  thrpt   45  12.736 ± 0.561  ops/us
SequentialQueueBench.twoThreads        Combiner  thrpt   45  19.331 ± 0.371  ops/us
* */
public class SequentialQueueBench {

    private Queue<Integer> queue;
    @Param({"JDK", "Combiner"})
    private String type;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean enqueue = true;
    }


    @Setup
    public void setup() {
        queue = type.equals("JDK") ? new ConcurrentLinkedQueue<>() : Combiners.queue(new FlatCombiner<>(new ArrayDeque<>(), 20, 500), WaitStrategy.park(1));
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

    @Threads(16)
    @Benchmark
    public void sixteenThreads(Blackhole bh, ThreadState ts) {
        addOrRemove(bh, ts);
    }

    @Threads(32)
    @Benchmark
    public void thirtyTwoThreads(Blackhole bh, ThreadState ts) {
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
                    .include(SequentialQueueBench.class.getSimpleName())
                    .addProfiler(JavaFlightRecorderProfiler.class, "dir=C:\\jfr-fc")
                    .build();
            new org.openjdk.jmh.runner.Runner(options).run();
        }
    }
}