package io.github.kusoroadeolu.fc.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

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
* Benchmark                   Mode  Cnt   Score   Error   Units
MSQueueBench.eightThreads  thrpt   45  10.795 ± 0.124  ops/us
MSQueueBench.fourThreads   thrpt   45   9.929 ± 0.111  ops/us
MSQueueBench.twoThreads    thrpt   45  11.270 ± 0.371  ops/us
* */

/* Latency
* Benchmark                  Mode  Cnt  Score   Error  Units
MSQueueBench.eightThreads  avgt   45  0.783 ± 0.008  us/op
MSQueueBench.fourThreads   avgt   45  0.445 ± 0.009  us/op
MSQueueBench.twoThreads    avgt   45  0.159 ± 0.005  us/op
* */
public class MSQueueBench {

    private ConcurrentLinkedQueue<Integer> queue;

    @State(Scope.Thread)
    public static class ThreadState {
        boolean enqueue = true;
    }

    @Setup
    public void setup() {
        queue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 1000; i++) queue.offer(i);
    }

    @Threads(2)
    @Benchmark
    public void twoThreads(Blackhole bh, ThreadState ts) {
        boolean isEnqueue = ts.enqueue;
        ts.enqueue = !isEnqueue;
        if (isEnqueue) bh.consume(queue.offer(42));
        else bh.consume(queue.poll());
    }

    @Threads(4)
    @Benchmark
    public void fourThreads(Blackhole bh, ThreadState ts) {
        boolean isEnqueue = ts.enqueue;
        ts.enqueue = !isEnqueue;
        if (isEnqueue) bh.consume(queue.offer(42));
        else bh.consume(queue.poll());
    }

    @Threads(8)
    @Benchmark
    public void eightThreads(Blackhole bh, ThreadState ts) {
        boolean isEnqueue = ts.enqueue;
        ts.enqueue = !isEnqueue;
        if (isEnqueue) bh.consume(queue.offer(42));
        else bh.consume(queue.poll());
    }
}