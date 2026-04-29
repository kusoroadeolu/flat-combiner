package io.github.kusoroadeolu.fc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class CombiningSet<T> extends CombiningCollection<T> implements  Set<T> {
    private final Combiner<Set<T>> combiner;
    private final WaitStrategy waitStrategy;

    public CombiningSet() {
        this(new FlatCombiner<>(new HashSet<>()), WaitStrategy.park(1));
    }

    public CombiningSet(Combiner<Set<T>> combiner, WaitStrategy waitStrategy) {
        this.combiner = Objects.requireNonNull(combiner);
        this.waitStrategy = Objects.requireNonNull(waitStrategy);
    }

    @Override
   public Combiner<? extends Collection<T>> combiner() {
        return combiner;
    }

    @Override
   public WaitStrategy waitStrategy() {
        return waitStrategy;
    }
}
