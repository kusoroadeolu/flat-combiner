package io.github.kusoroadeolu.fc;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class Combiners {
    private Combiners(){}

    public static <T> Set<T> set(){
        return new CombinerSet<>();
    }

    public static <T> Set<T> set(Combiner<Set<T>> combiner, WaitStrategy waitStrategy){
        return new CombinerSet<>(combiner, waitStrategy);
    }

    public static <T> Queue<T> queue(){
        return new CombinerQueue<>();
    }

    public static <T> Queue<T> queue(Combiner<Queue<T>> combiner, WaitStrategy waitStrategy){
        return new CombinerQueue<>(combiner, waitStrategy);
    }

    public static <T> List<T> list(){
        return new CombinerList<>();
    }

    public static <T> List<T> list(Combiner<List<T>> combiner, WaitStrategy waitStrategy){
        return new CombinerList<>(combiner, waitStrategy);
    }


}
