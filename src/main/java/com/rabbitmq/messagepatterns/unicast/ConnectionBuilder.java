package com.rabbitmq.messagepatterns.unicast;

import com.rabbitmq.client.Connection;

public interface ConnectionBuilder {
    public Connection createConnection();
}
