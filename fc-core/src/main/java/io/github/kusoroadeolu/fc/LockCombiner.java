package io.github.kusoroadeolu.fc;

import java.util.Objects;
import java.util.function.Consumer;

public class LockCombiner<T> implements Combiner<T> {
    private final Object lock;
    private final T item;

    public LockCombiner(T item) {
        this.lock = new Object();
        this.item = Objects.requireNonNull(item);
    }

    @Override
    public int combine(Consumer<T> action) {
        synchronized (lock){
            action.accept(item);
        }

        return -1; //Lock based combiner doesnt matter
    }
}
