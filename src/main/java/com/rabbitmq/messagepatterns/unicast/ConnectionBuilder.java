package com.rabbitmq.messagepatterns.unicast;

import com.rabbitmq.client.Connection;

import java.io.IOException;

public interface ConnectionBuilder {
    public Connection createConnection() throws IOException;
}
