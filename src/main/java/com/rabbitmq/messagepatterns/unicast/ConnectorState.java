package com.rabbitmq.messagepatterns.unicast;

/**
 * Enumeration of the possible states that a connector could be in.
 */
public enum ConnectorState {
    /**
     * The endpoint is connected to an AMQP server.
     */
    CONNECTED,

    /**
     * The endpoint is not connected to any AMQP server, but is attempting to reconnect.
     */
    RECONNECTING,

    /**
     * The endpoint is not connected to any AMQP server, and is not currently attempting
     * to reconnect.
     */
    DISCONNECTED
}
