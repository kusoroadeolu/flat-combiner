package io.github.kusoroadeolu.fc;

import java.util.*;

abstract class CombinerCollection<T> implements Collection<T> {

    @Override
    public final int size() {
        return combiner().combine(Collection::size);
    }

    @Override
    public final boolean isEmpty() {
        return combiner().combine(Collection::isEmpty);
    }

    @Override
    public final boolean contains(Object o) {
        return combiner().combine(l -> l.contains(o));
    }

    @Override
    public final Iterator<T> iterator() {
        return combiner().combine(Collection::iterator);
    }

    @Override
    public final Object[] toArray() {
        return combiner().combine(Collection::toArray);
    }

    @Override
    public final  <T1> T1[] toArray(T1[] a) {
        return combiner().combine(l -> l.toArray(a));
    }

    @Override
    public final boolean add(T t) {
        return combiner().combine(l -> l.add(t));
    }

    @Override
    public final boolean remove(Object o) {
        return combiner().combine(l -> l.remove(o));
    }

    @Override
    public final boolean containsAll(Collection<?> c) {
        return combiner().combine(l -> new HashSet<>(l).containsAll(c));
    }

    @Override
    public final boolean addAll(Collection<? extends T> c) {
        return combiner().combine(l -> l.addAll(c));
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
        return combiner().combine(l -> l.removeAll(c));
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
        return combiner().combine(l -> l.retainAll(c));
    }

    @Override
    public final void clear() {
        combiner().combine(l -> {
            l.clear();
            return null;
        });
    }


    abstract Combiner<? extends Collection<T>> combiner();
}
