package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.HandOffCombiner;
import io.github.kusoroadeolu.fc.WaitStrategy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
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

//Measures flat combining with a park wait strategy against the JDK lock free concurrent linked queue

// Combiner = flat combiner
/* Thrpt
* Benchmark                                (type)   Mode  Cnt   Score   Error   Units
SequentialQueueBench.eightThreads           JDK  thrpt   45   9.952 ± 0.155  ops/us
SequentialQueueBench.eightThreads      Combiner  thrpt   45  25.189 ± 1.086  ops/us
SequentialQueueBench.fourThreads            JDK  thrpt   45   8.936 ± 0.149  ops/us
SequentialQueueBench.fourThreads       Combiner  thrpt   45  24.848 ± 0.475  ops/u
* */

/* Thrpt using @Contended
* Benchmark                           Mode  Cnt   Score   Error   Units
SequentialQueueBench.eightThreads  thrpt   45  22.703 ± 1.303  ops/us
SequentialQueueBench.fourThreads   thrpt   45  25.290 ± 0.797  ops/us
* */



/*
* Latency
* Benchmark                                (type)  Mode  Cnt  Score   Error  Units
SequentialQueueBench.eightThreads           JDK  avgt   45  0.824 ± 0.014  us/op
SequentialQueueBench.eightThreads      Combiner  avgt   45  0.454 ± 0.010  us/op
SequentialQueueBench.fourThreads            JDK  avgt   45  0.441 ± 0.005  us/op
SequentialQueueBench.fourThreads       Combiner  avgt   45  0.255 ± 0.027  us/op
* */

/* Latency using @Contended
* Benchmark                          Mode  Cnt  Score   Error  Units
SequentialQueueBench.eightThreads  avgt   45  0.358 ± 0.016  us/op
SequentialQueueBench.fourThreads   avgt   45  0.171 ± 0.007  us/op
* Latency actually reduced pretty significantly
*
* */

/* Handoff combiner
* Benchmark                           Mode  Cnt   Score   Error   Units
SequentialQueueBench.eightThreads  thrpt   30  25.972 ± 1.690  ops/us
SequentialQueueBench.fourThreads   thrpt   30  25.324 ± 1.213  ops/us
* */

/* Handoff combiner with padding with @contended
* Benchmark                           Mode  Cnt   Score   Error   Units
SequentialQueueBench.eightThreads  thrpt   30  25.583 ± 1.241  ops/us
SequentialQueueBench.fourThreads   thrpt   30  24.061 ± 0.868  ops/us
* */

/* Handoff combiner latency
* Benchmark                          Mode  Cnt  Score   Error  Units
SequentialQueueBench.eightThreads  avgt   30  0.358 ± 0.014  us/op
SequentialQueueBench.fourThreads   avgt   30  0.171 ± 0.011  us/op
* */


/* Handoff combiner latency with contended
* Benchmark                          Mode  Cnt  Score   Error  Units
SequentialQueueBench.eightThreads  avgt   30  0.568 ± 0.097  us/op
SequentialQueueBench.fourThreads   avgt   30  0.325 ± 0.016  us/op
* */

public class SequentialQueueBench {

    private Queue<Integer> queue;
    @Param({ "JDK", "Handoff", "Combiner"})
    private String type;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean enqueue = true;
    }


    @Setup
    public void setup() {
        queue = switch (type){
            case "JDK" -> new ConcurrentLinkedQueue<>();
            case "Combiner" -> Combiners.queue();
            case "Handoff" -> Combiners.queue(new HandOffCombiner<>(new ArrayDeque<>()), WaitStrategy.park(1));
            default -> throw new RuntimeException();
        };

        for (int i = 0; i < 1000; i++) queue.offer(i);
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