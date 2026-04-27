package io.github.kusoroadeolu.fc;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Objects;
import java.util.Queue;

class CombinerQueue<T> extends CombinerCollection<T> implements Queue<T> {
    private final Combiner<Queue<T>> combiner;
    private final WaitStrategy waitStrategy;

    public CombinerQueue() {
        this(new FlatCombiner<>(new ArrayDeque<>()), WaitStrategy.park(1));
    }

    public CombinerQueue(Combiner<Queue<T>> combiner, WaitStrategy strategy) {
        this.combiner = Objects.requireNonNull(combiner);
        this.waitStrategy = Objects.requireNonNull(strategy);
    }

    @Override
    public boolean offer(T t) {
        return combiner.combine(q -> q.offer(t), waitStrategy);
    }

    @Override
    public T remove() {
        return combiner.combine(Queue::remove, waitStrategy);
    }

    @Override
    public T poll() {
        return combiner.combine(Queue::poll, waitStrategy);
    }

    @Override
    public T element() {
        return combiner.combine(Queue::element, waitStrategy);
    }

    @Override
    public T peek() {
        return combiner.combine(Queue::peek, waitStrategy);
    }

    @Override
    Combiner<? extends Collection<T>> combiner() {
        return combiner;
    }

    @Override
    WaitStrategy waitStrategy() {
        return waitStrategy;
    }
}
