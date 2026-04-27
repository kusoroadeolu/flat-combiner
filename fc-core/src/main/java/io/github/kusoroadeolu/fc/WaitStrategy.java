package io.github.kusoroadeolu.fc;

import java.util.concurrent.locks.LockSupport;

public interface WaitStrategy {
    void idle();

    static WaitStrategy yield(){
        return Thread::yield;
    }

    static WaitStrategy spinWait(){
        return Thread::onSpinWait;
    }

    static WaitStrategy park(long nanos) {
        return () -> LockSupport.parkNanos(nanos);
    }
}
