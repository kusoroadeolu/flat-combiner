package io.github.kusoroadeolu.fc;

import org.jetbrains.lincheck.Lincheck;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class LincheckTests {

    @Test
    public void sizeTest() {
        Lincheck.runConcurrentTest(() -> {
            var list = new ArrayList<Integer>();
            final Combiner<List<Integer>> combiner = new FlatCombiner<>(list);
            Thread t1 = new Thread(() -> combiner.combine(l -> l.add(1)));
            Thread t2 = new Thread(() -> combiner.combine(l -> l.add(2)));
            Thread t3 = new Thread(() -> combiner.combine(l -> l.add(3)));

            t1.start();
            t2.start();
            t3.start();

            try {
                t1.join();
                t2.join();
                t3.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            Assertions.assertEquals(3, list.size());
        });
    }

    @Test
    public void noLostWrites() {
        Lincheck.runConcurrentTest(() -> {
            var list = new ArrayList<Integer>();
            final Combiner<List<Integer>> combiner = new FlatCombiner<>(list);
            Thread t1 = new Thread(() -> combiner.combine(l -> l.add(1)));
            Thread t2 = new Thread(() -> combiner.combine(l -> l.add(2)));
            Thread t3 = new Thread(() -> combiner.combine(l -> l.add(3)));

            t1.start();
            t2.start();
            t3.start();

            try {
                t1.join();
                t2.join();
                t3.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            Assertions.assertTrue(list.contains(1));
            Assertions.assertTrue(list.contains(2));
            Assertions.assertTrue(list.contains(3));
        });
    }
}
