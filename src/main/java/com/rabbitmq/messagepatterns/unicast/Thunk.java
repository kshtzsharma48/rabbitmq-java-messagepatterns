package com.rabbitmq.messagepatterns.unicast;

/**
 * Since Runnable can't throw a checked exception
 */
public interface Thunk {
    public void run() throws Exception;
}
