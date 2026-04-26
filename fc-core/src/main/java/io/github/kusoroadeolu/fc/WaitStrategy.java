package io.github.kusoroadeolu.fc;

public interface WaitStrategy {
    void idle();

    static WaitStrategy yield(){
        return Thread::yield;
    }

    static WaitStrategy spinWait(){
        return Thread::onSpinWait;
    }
}
