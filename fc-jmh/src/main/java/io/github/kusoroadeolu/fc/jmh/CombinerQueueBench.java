package io.github.kusoroadeolu.fc.jmh;

import io.github.kusoroadeolu.fc.Combiner;
import io.github.kusoroadeolu.fc.FlatCombiner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 15, time = 1)
@Fork(3)
/*
* Benchmarks the flat combining approach against a plain lock to see if batching amortizes the cost of acquiring a lock
* */

//UnPadded Combiner
/*
* Benchmark                                          (type)   Mode  Cnt   Score    Error   Units
CombinerQueueBench.eightThreads                  combiner  thrpt   45  18.427 ±  0.380  ops/us
CombinerQueueBench.eightThreads:avgBatchSize     combiner  thrpt   45  ≈ 10⁻⁵           ops/us
CombinerQueueBench.eightThreads:batchSize1       combiner  thrpt   45   0.002 ±  0.001  ops/us
CombinerQueueBench.eightThreads:batchSize2to5    combiner  thrpt   45   0.014 ±  0.006  ops/us
CombinerQueueBench.eightThreads:batchSize6to20   combiner  thrpt   45   2.360 ±  0.037  ops/us
CombinerQueueBench.eightThreads:totalBatchSize   combiner  thrpt   45  18.532 ±  0.371  ops/us
CombinerQueueBench.eightThreads:totalOps         combiner  thrpt   45  18.531 ±  0.373  ops/us
CombinerQueueBench.fourThreads                   combiner  thrpt   45  12.756 ±  0.305  ops/us
CombinerQueueBench.fourThreads:avgBatchSize      combiner  thrpt   45  ≈ 10⁻⁵           ops/us
CombinerQueueBench.fourThreads:batchSize1        combiner  thrpt   45   0.521 ±  0.056  ops/us
CombinerQueueBench.fourThreads:batchSize2to5     combiner  thrpt   45   3.392 ±  0.092  ops/us
CombinerQueueBench.fourThreads:batchSize6to20    combiner  thrpt   45     ≈ 0           ops/us
CombinerQueueBench.fourThreads:totalBatchSize    combiner  thrpt   45  12.757 ±  0.305  ops/us
CombinerQueueBench.fourThreads:totalOps          combiner  thrpt   45  12.757 ±  0.305  ops/us
CombinerQueueBench.twoThreads                    combiner  thrpt   45  14.409 ±  0.613  ops/us
CombinerQueueBench.twoThreads:avgBatchSize       combiner  thrpt   45  ≈ 10⁻⁶           ops/us
CombinerQueueBench.twoThreads:batchSize1         combiner  thrpt   45   6.557 ±  0.354  ops/us
CombinerQueueBench.twoThreads:batchSize2to5      combiner  thrpt   45   3.926 ±  0.140  ops/us
CombinerQueueBench.twoThreads:batchSize6to20     combiner  thrpt   45     ≈ 0           ops/us
CombinerQueueBench.twoThreads:totalBatchSize     combiner  thrpt   45  14.409 ±  0.613  ops/us
CombinerQueueBench.twoThreads:totalOps           combiner  thrpt   45  14.409 ±  0.613  ops/us
*
* */

/*
* Benchmark                                       Mode  Cnt        Score        Error  Units
CombinerQueueBench.eightThreads                 avgt   45        0.440 ±      0.005  us/op
CombinerQueueBench.fourThreads                  avgt   45        0.308 ±      0.009  us/op
CombinerQueueBench.twoThreads                   avgt   45        0.134 ±      0.007  us/op
* */

// Padded combiner
/*
*  Benchmark                                         Mode  Cnt   Score    Error   Units
CombinerQueueBench.eightThreads                  thrpt   45  17.294 ±  0.309  ops/us
CombinerQueueBench.eightThreads:avgBatchSize     thrpt   45  ≈ 10⁻⁵           ops/us
CombinerQueueBench.eightThreads:batchSize1       thrpt   45   0.001 ±  0.001  ops/us
CombinerQueueBench.eightThreads:batchSize2to5    thrpt   45   0.014 ±  0.004  ops/us
CombinerQueueBench.eightThreads:batchSize6to20   thrpt   45   2.205 ±  0.039  ops/us
CombinerQueueBench.eightThreads:totalBatchSize   thrpt   45  17.383 ±  0.315  ops/us
CombinerQueueBench.eightThreads:totalOps         thrpt   45  17.384 ±  0.315  ops/us
CombinerQueueBench.fourThreads                   thrpt   45  12.214 ±  0.443  ops/us
CombinerQueueBench.fourThreads:avgBatchSize      thrpt   45  ≈ 10⁻⁵           ops/us
CombinerQueueBench.fourThreads:batchSize1        thrpt   45   0.906 ±  0.116  ops/us
CombinerQueueBench.fourThreads:batchSize2to5     thrpt   45   3.191 ±  0.117  ops/us
CombinerQueueBench.fourThreads:batchSize6to20    thrpt   45     ≈ 0           ops/us
CombinerQueueBench.fourThreads:totalBatchSize    thrpt   45  12.214 ±  0.443  ops/us
CombinerQueueBench.fourThreads:totalOps          thrpt   45  12.214 ±  0.443  ops/us
CombinerQueueBench.twoThreads                    thrpt   45  16.300 ±  0.980  ops/us
CombinerQueueBench.twoThreads:avgBatchSize       thrpt   45  ≈ 10⁻⁶           ops/us
CombinerQueueBench.twoThreads:batchSize1         thrpt   45   7.770 ±  0.512  ops/us
CombinerQueueBench.twoThreads:batchSize2to5      thrpt   45   4.266 ±  0.253  ops/us
CombinerQueueBench.twoThreads:batchSize6to20     thrpt   45     ≈ 0           ops/us
CombinerQueueBench.twoThreads:totalBatchSize     thrpt   45  16.301 ±  0.980  ops/us
CombinerQueueBench.twoThreads:totalOps           thrpt   45  16.301 ±  0.980  ops/us
* */

/* Latency
* Benchmark                                        Mode  Cnt        Score        Error  Units
CombinerQueueBench.eightThreads                  avgt   45        0.489 ±      0.009  us/op
CombinerQueueBench.fourThreads                   avgt   45        0.328 ±      0.008  us/op
CombinerQueueBench.twoThreads                    avgt   45        0.181 ±      0.021  us/op
* */

// Lock based
/*
CombinerQueueBench.fourThreads:totalOps              lock  thrpt   45  21.667 ±  2.347  ops/us
CombinerQueueBench.eightThreads:totalOps             lock  thrpt   45  22.054 ±  1.276  ops/us
CombinerQueueBench.twoThreads:totalOps               lock  thrpt   45  19.896 ±  1.495  ops/us
* */

/*
*
* At 2 threads, batchSize1 dominates threads are barely combining.
* By 8 threads batchSize6to20 becomes the dominant bucket at 2.360 ops/us.
*  So combining is actually kicking in at higher thread counts,
* which is why FC scales up from 14→18 while MS-queue stays flat.
*
* There is still some more areas to improve my flat combiner i.e. reducing the risk of false sharing across nodes, since threads spin on their nodes
* */
public class CombinerQueueBench {

    private Combiner<Queue<Integer>> combiner;


    @State(Scope.Thread)
    public static class ThreadState {
        boolean enqueue = true;
    }

    @AuxCounters
    @State(Scope.Thread)
    public static class BatchCounters {
        public long totalOps;
        public long totalBatchSize;
        public long batchSize1;
        public long batchSize2to5;
        public long batchSize6to20;

        public double avgBatchSize() {
            return totalOps == 0 ? 0 : (double) totalBatchSize / totalOps;
        }
    }

    @Setup
    public void setup() {
        Queue<Integer> queue = new ArrayDeque<>();
        // Pre-fill so dequeues don't always hit empty
        for (int i = 0; i < 1000; i++) queue.offer(i);
        combiner = new FlatCombiner<>(queue);
    }

    private void trackBatch(int batch, BatchCounters counters) {
        counters.totalOps++;
        if (batch > 0) {
            counters.totalBatchSize += batch;
            if (batch == 1)          counters.batchSize1++;
            else if (batch <= 5)     counters.batchSize2to5++;
            else if (batch <= 20)    counters.batchSize6to20++;
        }
    }

    @Threads(2)
    @Benchmark
    public void twoThreads(Blackhole bh, ThreadState ts, BatchCounters counters) {
        enqueueAndRecord(bh, ts, counters);
    }

    @Threads(4)
    @Benchmark
    public void fourThreads(Blackhole bh, ThreadState ts, BatchCounters counters) {
        enqueueAndRecord(bh, ts, counters);
    }

    private void enqueueAndRecord(Blackhole bh, ThreadState ts, BatchCounters counters) {
        boolean isEnqueue = ts.enqueue;
        ts.enqueue = !isEnqueue;
        int batch = isEnqueue
                ? combiner.combine(q -> q.offer(42))
                : combiner.combine(Queue::poll);
        trackBatch(batch, counters);
        bh.consume(batch);
    }

    @Threads(8)
    @Benchmark
    public void eightThreads(Blackhole bh, ThreadState ts, BatchCounters counters) {
        enqueueAndRecord(bh, ts, counters);
    }
}