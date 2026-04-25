package io.github.kusoroadeolu.fc;

import java.util.function.Consumer;

public interface Combiner<T> {
    int combine(Consumer<T> action);

}
