package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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
@Measurement(iterations = 15, time = 1)
@Fork(3)

//Baseline bench as per the paper, measures flat combining approach against a lock free linear structure
/* Thrpt
Benchmark                                (type)   Mode  Cnt   Score   Error   Units
SequentialQueueBench.fourThreads            JDK  thrpt   45   9.707 ± 0.194  ops/us
SequentialQueueBench.fourThreads       Combiner  thrpt   45   9.079 ± 0.492  ops/us
SequentialQueueBench.sixteenThreads         JDK  thrpt   45  11.475 ± 0.213  ops/us
SequentialQueueBench.sixteenThreads    Combiner  thrpt   45   5.369 ± 0.444  ops/us
SequentialQueueBench.thirtyTwoThreads       JDK  thrpt   45  11.217 ± 0.084  ops/us
SequentialQueueBench.thirtyTwoThreads  Combiner  thrpt   45   2.425 ± 0.365  ops/us
SequentialQueueBench.twoThreads             JDK  thrpt   45  13.223 ± 0.389  ops/us
SequentialQueueBench.twoThreads        Combiner  thrpt   45   9.692 ± 0.476  ops/us
SequentialQueueBench.eightThreads       JDK  thrpt   45  11.777 ± 0.066  ops/us
SequentialQueueBench.eightThreads  Combiner  thrpt   45  11.185 ± 0.319  ops/us
* */

/* Latency
* Benchmark                  Mode  Cnt  Score   Error  Units
SequentialQueueBench.eightThreads  avgt   45  0.783 ± 0.008  us/op
SequentialQueueBench.fourThreads   avgt   45  0.445 ± 0.009  us/op
SequentialQueueBench.twoThreads    avgt   45  0.159 ± 0.005  us/op
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
        queue = type.equals("JDK") ? new ConcurrentLinkedQueue<>() : Combiners.queue(new FlatCombiner<>(new ArrayDeque<>(), 20, 1000));
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
            Options options = new OptionsBuilder().include(SequentialQueueBench.class.getSimpleName()).build();
            new org.openjdk.jmh.runner.Runner(options).run();
        }
    }
}