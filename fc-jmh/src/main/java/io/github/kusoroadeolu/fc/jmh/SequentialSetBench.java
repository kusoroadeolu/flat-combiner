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

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(3)

/*
* Benchmark                              (type)   Mode  Cnt   Score   Error   Units
SequentialSetBench.eightThreads           JDK  thrpt   45  10.898 ± 0.267  ops/us
SequentialSetBench.eightThreads      Combiner  thrpt   45  21.540 ± 0.567  ops/us
SequentialSetBench.fourThreads            JDK  thrpt   45  11.207 ± 0.149  ops/us
SequentialSetBench.fourThreads       Combiner  thrpt   45  21.476 ± 0.598  ops/us
SequentialSetBench.sixteenThreads         JDK  thrpt   45  10.913 ± 0.124  ops/us
SequentialSetBench.sixteenThreads    Combiner  thrpt   45  19.772 ± 0.773  ops/us
SequentialSetBench.thirtyTwoThreads       JDK  thrpt   45  10.803 ± 0.165  ops/us
SequentialSetBench.thirtyTwoThreads  Combiner  thrpt   45  20.390 ± 0.456  ops/us
SequentialSetBench.twoThreads             JDK  thrpt   45  15.818 ± 0.575  ops/us
SequentialSetBench.twoThreads        Combiner  thrpt   45  20.929 ± 0.627  ops/us
* */

/* latency
* Benchmark                              (type)  Mode  Cnt  Score   Error  Units
SequentialSetBench.eightThreads           JDK  avgt   45  0.704 ± 0.013  us/op
SequentialSetBench.eightThreads      Combiner  avgt   45  0.525 ± 0.013  us/op
SequentialSetBench.fourThreads            JDK  avgt   45  0.340 ± 0.003  us/op
SequentialSetBench.fourThreads       Combiner  avgt   45  0.314 ± 0.043  us/op
SequentialSetBench.sixteenThreads         JDK  avgt   45  1.444 ± 0.028  us/op
SequentialSetBench.sixteenThreads    Combiner  avgt   45  1.121 ± 0.043  us/op
SequentialSetBench.thirtyTwoThreads       JDK  avgt   45  2.966 ± 0.050  us/op
SequentialSetBench.thirtyTwoThreads  Combiner  avgt   45  2.524 ± 0.133  us/op
SequentialSetBench.twoThreads             JDK  avgt   45  0.127 ± 0.004  us/op
SequentialSetBench.twoThreads        Combiner  avgt   45  0.133 ± 0.013  us/op
* */

public class SequentialSetBench {

    private Set<Integer> set;
    //@Param({"JDK", "Combiner"})
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
