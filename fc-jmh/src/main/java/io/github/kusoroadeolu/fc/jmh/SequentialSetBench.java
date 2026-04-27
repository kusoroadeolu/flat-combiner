package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiners;
import io.github.kusoroadeolu.fc.FlatCombiner;
import io.github.kusoroadeolu.fc.WaitStrategy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(3)

/*
* Benchmark                              (type)   Mode  Cnt   Score   Error   Units
SequentialSetBench.eightThreads           JDK  thrpt   45  10.898 ± 0.267  ops/us
SequentialSetBench.eightThreads      Combiner  thrpt   45  16.514 ± 0.413  ops/us
SequentialSetBench.fourThreads            JDK  thrpt   45  11.207 ± 0.149  ops/us
SequentialSetBench.fourThreads       Combiner  thrpt   45  16.777 ± 0.603  ops/us
SequentialSetBench.sixteenThreads         JDK  thrpt   45  10.913 ± 0.124  ops/us
SequentialSetBench.sixteenThreads    Combiner  thrpt   45  15.916 ± 0.539  ops/us
SequentialSetBench.thirtyTwoThreads       JDK  thrpt   45  10.803 ± 0.165  ops/us
SequentialSetBench.thirtyTwoThreads  Combiner  thrpt   45  15.833 ± 0.503  ops/us
SequentialSetBench.twoThreads             JDK  thrpt   45  15.818 ± 0.575  ops/us
SequentialSetBench.twoThreads        Combiner  thrpt   45  17.243 ± 0.338  ops/us
* */

//Run with \ WaitStrategy#SpinWait

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
        set = type.equals("JDK") ? ConcurrentHashMap.newKeySet() : Combiners.set(new FlatCombiner<>(new HashSet<>(), 20, 500), WaitStrategy.park(1));
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
