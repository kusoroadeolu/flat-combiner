package io.github.kusoroadeolu.fc;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class Combiners {
    private Combiners(){}

    public static <T> Set<T> set(){
        return new CombiningSet<>();
    }

    public static <T> Set<T> set(Combiner<Set<T>> combiner, WaitStrategy waitStrategy){
        return new CombiningSet<>(combiner, waitStrategy);
    }

    public static <T> Queue<T> queue(){
        return new CombiningQueue<>();
    }

    public static <T> Queue<T> queue(Combiner<Queue<T>> combiner, WaitStrategy waitStrategy){
        return new CombiningQueue<>(combiner, waitStrategy);
    }

    public static <T> List<T> list(){
        return new CombiningList<>();
    }

    public static <T> List<T> list(Combiner<List<T>> combiner, WaitStrategy waitStrategy){
        return new CombiningList<>(combiner, waitStrategy);
    }


}
