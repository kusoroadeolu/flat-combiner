package io.github.kusoroadeolu.fc;

import java.util.*;

class CombinerQueue<T> extends CombinerCollection<T> implements Queue<T> {
    private final Combiner<Queue<T>> combiner;
    public CombinerQueue() {
        this(new FlatCombiner<>(new ArrayDeque<>()));
    }

    public CombinerQueue(Combiner<Queue<T>> combiner) {
        this.combiner = Objects.requireNonNull(combiner);
    }


    @Override
    public boolean offer(T t) {
        return combiner.combine(q -> q.offer(t));
    }

    @Override
    public T remove() {
        return combiner.combine(Queue::remove);
    }

    @Override
    public T poll() {
        return combiner.combine(Queue::poll);
    }

    @Override
    public T element() {
        return combiner.combine(Queue::element);
    }

    @Override
    public T peek() {
        return combiner.combine(Queue::peek);
    }

    @Override
    Combiner<? extends Collection<T>> combiner() {
        return combiner;
    }
}
