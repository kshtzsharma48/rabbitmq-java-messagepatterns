package com.rabbitmq.messagepatterns.unicast;

/**
 * Life cycle definition
 */
public interface Disposable {

    /**
     * Initiates the a clean shutdown of the participating delegate 
     */
    void terminate() throws Exception;
}
