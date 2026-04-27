package io.github.kusoroadeolu.fc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

abstract class CombinerCollection<T> implements Collection<T> {

    @Override
    public final int size() {
        return combiner().combine(Collection::size, waitStrategy());
    }

    @Override
    public final boolean isEmpty() {
        return combiner().combine(Collection::isEmpty, waitStrategy());
    }

    @Override
    public final boolean contains(Object o) {
        return combiner().combine(l -> l.contains(o), waitStrategy());
    }

    @Override
    public final Iterator<T> iterator() {
        return combiner().combine(Collection::iterator, waitStrategy());
    }

    @Override
    public final Object[] toArray() {
        return combiner().combine(Collection::toArray, waitStrategy());
    }

    @Override
    public final  <T1> T1[] toArray(T1[] a) {
        return combiner().combine(l -> l.toArray(a), waitStrategy());
    }

    @Override
    public final boolean add(T t) {
        return combiner().combine(l -> l.add(t), waitStrategy());
    }

    @Override
    public final boolean remove(Object o) {
        return combiner().combine(l -> l.remove(o), waitStrategy());
    }

    @Override
    public final boolean containsAll(Collection<?> c) {
        return combiner().combine(l -> new HashSet<>(l).containsAll(c), waitStrategy());
    }

    @Override
    public final boolean addAll(Collection<? extends T> c) {
        return combiner().combine(l -> l.addAll(c), waitStrategy());
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
        return combiner().combine(l -> l.removeAll(c), waitStrategy());
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
        return combiner().combine(l -> l.retainAll(c), waitStrategy());
    }

    @Override
    public final void clear() {
        combiner().combine(l -> {
            l.clear();
            return null;
        });
    }


    abstract Combiner<? extends Collection<T>> combiner();
    abstract WaitStrategy waitStrategy();
}
