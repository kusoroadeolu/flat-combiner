# Flat Combiner
This project offers to include a synchronization primitive based on the [original 2010 paper](https://people.csail.mit.edu/shanir/publications/Flat%20Combining%20SPAA%2010.pdf) by Hendler et al.

## Introduction
The core idea of flat combining is the cost of obtaining a lock to a shared data structure is amortized by threads publishing a request to a publication list and a combiner (a thread that acquired the lock)is made aware of requests published by other waiting threads by scanning a shared publication list

## Usage 
This library exposes a `FlatCombiner` class through a `Combiner` interface and three sequential structures through their JDK interface that are quite competitive to inbuilt JDK concurrent structures.
Threads awaiting their result block till their result is made visible

- Using a raw combiner
```java
    Combiner<Integer> combiner = new FlatCombiner<>(0);
    int i = combiner.combine(i -> ++i);   
```

- Using an inbuilt `List` combiner
```java
    List<Integer> list = Combiners.list();
    list.add(1); 
```

- Using an inbuilt `Set` combiner
```java
    Set<Integer> set = Combiners.set();
    set.add(1); 
```

- Using an inbuilt `Queue` combiner
```java
    Queue<Integer> queue = Combiners.queue();
    queue.offer(1); 
```

## Benchmarks
This project is benchmarked using **JMH**. It includes benchmarks against the inbuilt JDK implementations for lock-free/lock-based `<= O(N)` structures.
The results are competitive against the JDK implementations up to number of threads = no. of available CPU cores.

### Running the benchmarks
You can run all the benchmarks as so.
```bash
    mvn clean package
    cd fc-jmh
    java -jar benchmark.jar 
```

## Testing
This project is tested using both **JUnit** and **JCStress** to ensure sequential and concurrent correctness.

### Running the tests
You can run all the JCStress tests as so.
```bash
    mvn clean package
    cd fc-stress
    java -jar jcstress.jar 
```

### Potential Improvements
There is still room for the improvement of this implementation regarding performance especially the linear scan in the publication queue

## LICENSE
MIT

