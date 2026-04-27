package io.github.kusoroadeolu.fc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class CombinerSet<T> extends CombinerCollection<T> implements Set<T> {
    private final Combiner<Set<T>> combiner;
    private final WaitStrategy waitStrategy;

    public CombinerSet() {
        this(new FlatCombiner<>(new HashSet<>()), WaitStrategy.park(1));
    }

    public CombinerSet(Combiner<Set<T>> combiner, WaitStrategy waitStrategy) {
        this.combiner = Objects.requireNonNull(combiner);
        this.waitStrategy = Objects.requireNonNull(waitStrategy);
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
