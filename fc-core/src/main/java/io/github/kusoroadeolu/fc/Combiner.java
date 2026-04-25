package io.github.kusoroadeolu.fc;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Combiner<T> {
    <R>R combine(Function<T, R> action);

}
