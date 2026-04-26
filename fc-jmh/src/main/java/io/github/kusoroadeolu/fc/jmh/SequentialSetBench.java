package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(3)

/*
* Benchmark                              (type)   Mode  Cnt   Score   Error   Units
SequentialSetBench.eightThreads           JDK  thrpt   45  12.063 ± 0.234  ops/us
SequentialSetBench.eightThreads      Combiner  thrpt   45   8.898 ± 0.238  ops/us
SequentialSetBench.fourThreads            JDK  thrpt   45  12.354 ± 0.146  ops/us
SequentialSetBench.fourThreads       Combiner  thrpt   45   8.732 ± 0.663  ops/us
SequentialSetBench.sixteenThreads         JDK  thrpt   45  12.663 ± 0.078  ops/us
SequentialSetBench.sixteenThreads    Combiner  thrpt   45   3.617 ± 0.278  ops/us
SequentialSetBench.thirtyTwoThreads       JDK  thrpt   45  12.360 ± 0.229  ops/us
SequentialSetBench.thirtyTwoThreads  Combiner  thrpt   45   1.688 ± 0.251  ops/us
SequentialSetBench.twoThreads             JDK  thrpt   45  16.548 ± 0.414  ops/us
SequentialSetBench.twoThreads        Combiner  thrpt   45   7.234 ± 0.212  ops/us
* */
public class SequentialSetBench {

    private Set<Integer> set;
    @Param({"JDK", "Combiner"})
    private String type;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean add = true;
    }

    @Setup
    public void setup() {
        set = type.equals("JDK") ? ConcurrentHashMap.newKeySet() : Combiners.set(new FlatCombiner<>(new HashSet<>(), 20, 1000));
        for (int i = 0; i < 1000; i++) set.add(i);
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
        addOrRemove(bh, ts);;
    }

    void addOrRemove(Blackhole bh, ThreadState ts){
        boolean isEnqueue = ts.add;
        ts.add = !isEnqueue;
        if (isEnqueue) bh.consume(set.add(42));
        else bh.consume(set.remove(42));
    }
}
