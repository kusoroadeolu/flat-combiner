package io.github.kusoroadeolu.fc.jmh.batch;

import io.github.kusoroadeolu.fc.Combiner;
import io.github.kusoroadeolu.fc.FlatCombiner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


/* UnPadded Combiner
* Benchmark                                                    Mode  Cnt   Score    Error   Units
CombinerPriorityQueueBench.eightThreads                     thrpt   45   7.979 ±  0.564  ops/us
CombinerPriorityQueueBench.eightThreads:avgBatchSize        thrpt   45  ≈ 10⁻⁵           ops/us
CombinerPriorityQueueBench.eightThreads:batchSize1          thrpt   45  ≈ 10⁻⁴           ops/us
CombinerPriorityQueueBench.eightThreads:batchSize2to5       thrpt   45   0.006 ±  0.003  ops/us
CombinerPriorityQueueBench.eightThreads:batchSize6to20      thrpt   45   1.020 ±  0.072  ops/us
CombinerPriorityQueueBench.eightThreads:totalBatchSize      thrpt   45   8.012 ±  0.572  ops/us
CombinerPriorityQueueBench.eightThreads:totalOps            thrpt   45   8.012 ±  0.572  ops/us
CombinerPriorityQueueBench.fourThreads                      thrpt   45   6.915 ±  0.324  ops/us
CombinerPriorityQueueBench.fourThreads:avgBatchSize         thrpt   45  ≈ 10⁻⁵           ops/us
CombinerPriorityQueueBench.fourThreads:batchSize1           thrpt   45   0.025 ±  0.012  ops/us
CombinerPriorityQueueBench.fourThreads:batchSize2to5        thrpt   45   1.797 ±  0.102  ops/us
CombinerPriorityQueueBench.fourThreads:batchSize6to20       thrpt   45     ≈ 0           ops/us
CombinerPriorityQueueBench.fourThreads:totalBatchSize       thrpt   45   6.915 ±  0.324  ops/us
CombinerPriorityQueueBench.fourThreads:totalOps             thrpt   45   6.915 ±  0.324  ops/us
CombinerPriorityQueueBench.sixteenThreads                   thrpt   45   3.056 ±  0.284  ops/us
CombinerPriorityQueueBench.sixteenThreads:avgBatchSize      thrpt   45  ≈ 10⁻⁵           ops/us
CombinerPriorityQueueBench.sixteenThreads:batchSize1        thrpt   45  ≈ 10⁻⁴           ops/us
CombinerPriorityQueueBench.sixteenThreads:batchSize2to5     thrpt   45   0.002 ±  0.001  ops/us
CombinerPriorityQueueBench.sixteenThreads:batchSize6to20    thrpt   45   0.416 ±  0.037  ops/us
CombinerPriorityQueueBench.sixteenThreads:totalBatchSize    thrpt   45   3.284 ±  0.295  ops/us
CombinerPriorityQueueBench.sixteenThreads:totalOps          thrpt   45   3.284 ±  0.295  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads                 thrpt   45   1.283 ±  0.175  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:avgBatchSize    thrpt   45  ≈ 10⁻⁵           ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:batchSize1      thrpt   45  ≈ 10⁻⁴           ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:batchSize2to5   thrpt   45   0.003 ±  0.003  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:batchSize6to20  thrpt   45   0.195 ±  0.023  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:totalBatchSize  thrpt   45   1.544 ±  0.186  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:totalOps        thrpt   45   1.543 ±  0.187  ops/us
CombinerPriorityQueueBench.twoThreads                       thrpt   45   6.614 ±  0.415  ops/us
CombinerPriorityQueueBench.twoThreads:avgBatchSize          thrpt   45  ≈ 10⁻⁶           ops/us
CombinerPriorityQueueBench.twoThreads:batchSize1            thrpt   45   2.059 ±  0.313  ops/us
CombinerPriorityQueueBench.twoThreads:batchSize2to5         thrpt   45   2.277 ±  0.117  ops/us
CombinerPriorityQueueBench.twoThreads:batchSize6to20        thrpt   45     ≈ 0           ops/us
CombinerPriorityQueueBench.twoThreads:totalBatchSize        thrpt   45   6.614 ±  0.415  ops/us
CombinerPriorityQueueBench.twoThreads:totalOps              thrpt   45   6.614 ±  0.415  ops/us
* */


/* Padded combiner
* Benchmark                                                    Mode  Cnt   Score    Error   Units
CombinerPriorityQueueBench.eightThreads                     thrpt   45   7.567 ±  0.609  ops/us
CombinerPriorityQueueBench.eightThreads:avgBatchSize        thrpt   45  ≈ 10⁻⁵           ops/us
CombinerPriorityQueueBench.eightThreads:batchSize1          thrpt   45  ≈ 10⁻⁴           ops/us
CombinerPriorityQueueBench.eightThreads:batchSize2to5       thrpt   45   0.008 ±  0.004  ops/us
CombinerPriorityQueueBench.eightThreads:batchSize6to20      thrpt   45   0.973 ±  0.079  ops/us
CombinerPriorityQueueBench.eightThreads:totalBatchSize      thrpt   45   7.597 ±  0.611  ops/us
CombinerPriorityQueueBench.eightThreads:totalOps            thrpt   45   7.598 ±  0.611  ops/us
CombinerPriorityQueueBench.fourThreads                      thrpt   45   7.361 ±  0.513  ops/us
CombinerPriorityQueueBench.fourThreads:avgBatchSize         thrpt   45  ≈ 10⁻⁵           ops/us
CombinerPriorityQueueBench.fourThreads:batchSize1           thrpt   45   0.027 ±  0.012  ops/us
CombinerPriorityQueueBench.fourThreads:batchSize2to5        thrpt   45   1.968 ±  0.183  ops/us
CombinerPriorityQueueBench.fourThreads:batchSize6to20       thrpt   45     ≈ 0           ops/us
CombinerPriorityQueueBench.fourThreads:totalBatchSize       thrpt   45   7.361 ±  0.513  ops/us
CombinerPriorityQueueBench.fourThreads:totalOps             thrpt   45   7.361 ±  0.513  ops/us
CombinerPriorityQueueBench.sixteenThreads                   thrpt   45   2.863 ±  0.256  ops/us
CombinerPriorityQueueBench.sixteenThreads:avgBatchSize      thrpt   45  ≈ 10⁻⁵           ops/us
CombinerPriorityQueueBench.sixteenThreads:batchSize1        thrpt   45  ≈ 10⁻⁴           ops/us
CombinerPriorityQueueBench.sixteenThreads:batchSize2to5     thrpt   45   0.003 ±  0.001  ops/us
CombinerPriorityQueueBench.sixteenThreads:batchSize6to20    thrpt   45   0.392 ±  0.034  ops/us
CombinerPriorityQueueBench.sixteenThreads:totalBatchSize    thrpt   45   3.088 ±  0.268  ops/us
CombinerPriorityQueueBench.sixteenThreads:totalOps          thrpt   45   3.089 ±  0.268  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads                 thrpt   45   1.207 ±  0.170  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:avgBatchSize    thrpt   45  ≈ 10⁻⁵           ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:batchSize1      thrpt   45  ≈ 10⁻⁴           ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:batchSize2to5   thrpt   45   0.002 ±  0.002  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:batchSize6to20  thrpt   45   0.190 ±  0.025  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:totalBatchSize  thrpt   45   1.495 ±  0.196  ops/us
CombinerPriorityQueueBench.thirtyTwoThreads:totalOps        thrpt   45   1.495 ±  0.196  ops/us
CombinerPriorityQueueBench.twoThreads                       thrpt   45   7.240 ±  0.459  ops/us
CombinerPriorityQueueBench.twoThreads:avgBatchSize          thrpt   45  ≈ 10⁻⁶           ops/us
CombinerPriorityQueueBench.twoThreads:batchSize1            thrpt   45   2.461 ±  0.315  ops/us
CombinerPriorityQueueBench.twoThreads:batchSize2to5         thrpt   45   2.390 ±  0.106  ops/us
CombinerPriorityQueueBench.twoThreads:batchSize6to20        thrpt   45     ≈ 0           ops/us
CombinerPriorityQueueBench.twoThreads:totalBatchSize        thrpt   45   7.240 ±  0.459  ops/us
CombinerPriorityQueueBench.twoThreads:totalOps              thrpt   45   7.240 ±  0.459  ops/us
* */


//Removed the batch counters later on
//Prune threshold = 100, max combine pass = 100
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class CombinerPriorityQueueBench {

    private Combiner<PriorityQueue<Integer>> combiner;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean insert = true;
    }

    @AuxCounters
    @State(Scope.Thread)
    public static class BatchCounters {
        public long totalOps;
        public long totalBatchSize;
        public long batchSize1;
        public long batchSize2to5;
        public long batchSize6to20;
        public long batchSize21to50;
        public long batchSizeOver50;

        public double avgBatchSize() {
            return totalOps == 0 ? 0 : (double) totalBatchSize / totalOps;
        }
    }

    @Setup
    public void setup() {
        PriorityQueue<Integer> queue = new PriorityQueue<>();
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
            else if (batch <= 50)    counters.batchSize21to50++;
            else                     counters.batchSizeOver50++;
        }
    }



    @Threads(2)
    @Benchmark
    public void twoThreads(Blackhole bh, ThreadState ts, BatchCounters counters) {
        doWork(bh, ts, counters);
    }

    @Threads(4)
    @Benchmark
    public void fourThreads(Blackhole bh, ThreadState ts, BatchCounters counters) {
        doWork(bh, ts, counters);
    }

    @Threads(8)
    @Benchmark
    public void eightThreads(Blackhole bh, ThreadState ts, BatchCounters counters) {
        doWork(bh, ts, counters);
    }

    @Threads(16)
    @Benchmark
    public void sixteenThreads(Blackhole bh, ThreadState ts, BatchCounters counters) {
        doWork(bh, ts, counters);
    }

    @Threads(32)
    @Benchmark
    public void thirtyTwoThreads(Blackhole bh, ThreadState ts, BatchCounters counters) {
        doWork(bh, ts, counters);
    }

    private void doWork(Blackhole bh, ThreadState ts, BatchCounters counters) {
        boolean isInsert = ts.insert;
        ts.insert = !isInsert;
        Object batch;
        if (isInsert) batch = combiner.combine(pq -> pq.offer(ThreadLocalRandom.current().nextInt(10_000)));
        else batch = combiner.combine(PriorityQueue::poll);
//        trackBatch(batch, counters);
        bh.consume(batch);
    }

    static class Runner {
        static void main() throws RunnerException {
            Options options = new OptionsBuilder().include(CombinerPriorityQueueBench.class.getSimpleName()).build();
            new org.openjdk.jmh.runner.Runner(options).run();
        }
    }

}