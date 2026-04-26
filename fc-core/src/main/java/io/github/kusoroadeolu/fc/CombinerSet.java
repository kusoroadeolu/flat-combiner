package io.github.kusoroadeolu.fc;

import java.util.*;

class CombinerSet<T> extends CombinerCollection<T> implements Set<T> {
    private final Combiner<Set<T>> combiner;
    public CombinerSet() {
        this(new FlatCombiner<>(new HashSet<>()));
    }

    public CombinerSet(Combiner<Set<T>> combiner) {
        this.combiner = Objects.requireNonNull(combiner);
    }

    @Override
    Combiner<? extends Collection<T>> combiner() {
        return combiner;
    }
}
