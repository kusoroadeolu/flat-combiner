package io.github.kusoroadeolu.fc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;


//Sanity tests
class FlatCombinerTest {
    private FlatCombiner<List<Integer>> combiner;
    private List<Integer> list;

    @BeforeEach
    void setUp() {
        list = new ArrayList<>();
        combiner = new FlatCombiner<>(list);
    }

    @Test
    void assertCombinerAcceptsValues()  {
        combiner.combine(l -> l.add(1));
        assertTrue(list.contains(1));
    }


}