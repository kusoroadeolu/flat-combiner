package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import io.github.kusoroadeolu.fc.WaitStrategy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(3)

//Measures flat combining with a park wait strategy against the JDK lock free concurrent linked queue


/* Thrpt
* Benchmark                                (type)   Mode  Cnt   Score   Error   Units
SequentialQueueBench.eightThreads           JDK  thrpt   45   9.952 ± 0.155  ops/us
SequentialQueueBench.eightThreads      Combiner  thrpt   45  25.189 ± 1.086  ops/us
SequentialQueueBench.fourThreads            JDK  thrpt   45   8.936 ± 0.149  ops/us
SequentialQueueBench.fourThreads       Combiner  thrpt   45  24.848 ± 0.475  ops/us
SequentialQueueBench.twoThreads             JDK  thrpt   45  12.736 ± 0.561  ops/us
SequentialQueueBench.twoThreads        Combiner  thrpt   45  26.168 ± 0.333  ops/us
* */

/* Thrpt using @Contended
* Benchmark                           Mode  Cnt   Score   Error   Units
SequentialQueueBench.eightThreads  thrpt   45  22.703 ± 1.303  ops/us
SequentialQueueBench.fourThreads   thrpt   45  25.290 ± 0.797  ops/us
SequentialQueueBench.twoThreads    thrpt   45  23.589 ± 0.821  ops/us
* */

/*
* Latency
* Benchmark                                (type)  Mode  Cnt  Score   Error  Units
SequentialQueueBench.eightThreads           JDK  avgt   45  0.824 ± 0.014  us/op
SequentialQueueBench.eightThreads      Combiner  avgt   45  0.454 ± 0.010  us/op
SequentialQueueBench.fourThreads            JDK  avgt   45  0.441 ± 0.005  us/op
SequentialQueueBench.fourThreads       Combiner  avgt   45  0.255 ± 0.027  us/op
SequentialQueueBench.twoThreads             JDK  avgt   45  0.175 ± 0.011  us/op
SequentialQueueBench.twoThreads        Combiner  avgt   45  0.122 ± 0.011  us/op
* */

/* Latency using @Contended
* Benchmark                          Mode  Cnt  Score   Error  Units
SequentialQueueBench.eightThreads  avgt   45  0.358 ± 0.016  us/op
SequentialQueueBench.fourThreads   avgt   45  0.171 ± 0.007  us/op
SequentialQueueBench.twoThreads    avgt   45  0.078 ± 0.002  us/op
* Latency actually reduced pretty significantly
*
* */


public class SequentialQueueBench {

    private Queue<Integer> queue;
    // @Param({"JDK", "Combiner"})
    private String type;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean enqueue = true;
    }


    @Setup
    public void setup() {
         queue = /*type.equals("JDK") ? new ConcurrentLinkedQueue<>() :*/ Combiners.queue(new FlatCombiner<>(new ArrayDeque<>(), 20, 500), WaitStrategy.park(1));
        for (int i = 0; i < 1000; i++) queue.offer(i);
    }

    @Threads(2)
    @Benchmark
    public void twoThreads(Blackhole bh, ThreadState ts) {
        addOrRemove(bh, ts);
    }
    //https://www.baeldung.com/java-false-sharing-contended
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
                    .include(SequentialQueueBench.class.getSimpleName())
                    .addProfiler(JavaFlightRecorderProfiler.class, "dir=C:\\jfr-fc")
                    .build();
            new org.openjdk.jmh.runner.Runner(options).run();
        }
    }
}