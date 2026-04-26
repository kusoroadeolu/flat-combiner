package io.github.kusoroadeolu.fc;

import java.util.*;

/*
* A list backed by a combiner
* */
class CombinerList<T> extends CombinerCollection<T> implements List<T>{

    private final Combiner<List<T>> combiner;
    public CombinerList(Combiner<List<T>> combiner) {
        this.combiner = Objects.requireNonNull(combiner);
    }

    public CombinerList() {
        this.combiner = new FlatCombiner<>(new ArrayList<>());
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return combiner.combine(l -> l.addAll(index, c));
    }

    @Override
    public T get(int index) {
       return combiner.combine(l -> l.get(index));
    }

    @Override
    public T set(int index, T element) {
        return combiner.combine(l -> l.set(index, element));
    }

    @Override
    public void add(int index, T element) {
        combiner.combine(l -> {
            l.add(index, element);
            return null;
        });
    }

    @Override
    public T remove(int index) {
       return combiner.combine(l -> l.remove(index));
    }

    @Override
    public int indexOf(Object o) {
        return combiner.combine(l -> l.indexOf(o));
    }

    @Override
    public int lastIndexOf(Object o) {
        return combiner.combine(l -> l.lastIndexOf(o));
    }


    @Override
    public ListIterator<T> listIterator() {
        return combiner.combine(List::listIterator);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return combiner.combine(l -> l.listIterator(index));
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return combiner.combine(l -> l.subList(fromIndex, toIndex));
    }

    @Override
    Combiner<? extends Collection<T>> combiner() {
        return combiner;
    }
}
