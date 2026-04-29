package io.github.kusoroadeolu.fc;

import java.util.*;

/*
* A list backed by a combiner
* */
class CombiningList<T> extends CombiningCollection<T> implements List<T>{

    private final Combiner<List<T>> combiner;
    private final WaitStrategy waitStrategy;
    public CombiningList(Combiner<List<T>> combiner, WaitStrategy waitStrategy) {
        this.combiner = Objects.requireNonNull(combiner);
        this.waitStrategy = Objects.requireNonNull(waitStrategy);
    }



    public CombiningList() {
        this(new FlatCombiner<>(new ArrayList<>()), WaitStrategy.park(1));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return combiner.combine(l -> l.addAll(index, c), waitStrategy);
    }

    @Override
    public T get(int index) {
       return combiner.combine(l -> l.get(index), waitStrategy);
    }

    @Override
    public T set(int index, T element) {
        return combiner.combine(l -> l.set(index, element), waitStrategy);
    }

    @Override
    public void add(int index, T element) {
        combiner.combine(l -> {
            l.add(index, element);
            return null;
        }, waitStrategy);
    }

    @Override
    public T remove(int index) {
       return combiner.combine(l -> l.remove(index), waitStrategy);
    }

    @Override
    public int indexOf(Object o) {
        return combiner.combine(l -> l.indexOf(o), waitStrategy);
    }

    @Override
    public int lastIndexOf(Object o) {
        return combiner.combine(l -> l.lastIndexOf(o), waitStrategy);
    }


    @Override
    public ListIterator<T> listIterator() {
        return combiner.combine(List::listIterator, waitStrategy);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return combiner.combine(l -> l.listIterator(index), waitStrategy);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return combiner.combine(l -> l.subList(fromIndex, toIndex), waitStrategy);
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
