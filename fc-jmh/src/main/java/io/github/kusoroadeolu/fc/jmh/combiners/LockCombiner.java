package io.github.kusoroadeolu.fc.jmh.combiners;

import io.github.kusoroadeolu.fc.Combiner;

import java.util.Objects;
import java.util.function.Function;

public class LockCombiner<T>  {
    private final Object lock;
    private final T item;

    public LockCombiner(T item) {
        this.lock = new Object();
        this.item = Objects.requireNonNull(item);
    }

    public <R>R combine(Function<T, R> action) {
        synchronized (lock){
           return action.apply(item);
        }
    }
}
