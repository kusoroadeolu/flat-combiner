package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/*
Initial rough results
* Benchmark                    Mode  Cnt        Score        Error  Units
CombinerBench.eightThreads  thrpt   25  2891416.211 ± 395222.058  ops/s
CombinerBench.fourThreads   thrpt   25  3622432.374 ± 393844.445  ops/s
CombinerBench.twoThreads    thrpt   25  4395401.110 ± 514275.355  ops/s
* */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
public class CombinerBench {
    private Combiner<?> combiner;


    @AuxCounters
    @State(Scope.Thread)
    public static class BatchCounters {
        public long highBatches;  // > 20
        public long lowBatches;    // 1 < x < 20
    }

    @Setup
    public void setup(){
        combiner = new Combiner<>(0);
    }


    @Threads(2)
    @Benchmark
    public void twoThreads(Blackhole bh, BatchCounters counters){
        var batch = combiner.combine(e -> Blackhole.consumeCPU(100));
        if (batch > 0 && batch <= 20) counters.lowBatches++;
        else if (batch > 20) counters.highBatches++;
        bh.consume(batch);
    }


    @Threads(4)
    @Benchmark
    public void fourThreads(Blackhole bh, BatchCounters counters){
        var batch = combiner.combine(e -> Blackhole.consumeCPU(100));
        if (batch > 0 && batch <= 20) counters.lowBatches++;
        else if (batch > 20) counters.highBatches++;
        bh.consume(batch);
    }


    @Threads(8)
    @Benchmark
    public void eightThreads(Blackhole bh, BatchCounters counters){
        var batch = combiner.combine(e -> Blackhole.consumeCPU(100));
        if (batch > 0 && batch <= 20) counters.lowBatches++;
        else if (batch > 20) counters.highBatches++;
        bh.consume(batch);
    }


    public static class CombinerRunner {
        void main() throws Exception {
            Options opt = new OptionsBuilder()
                    .include(CombinerBench.class.getSimpleName())
                    .build();
            new Runner(opt).run();
        }
    }
}
