package com.rabbitmq.messagepatterns.unicast;

import com.rabbitmq.client.Connection;

import java.io.IOException;

public interface ConnectionListener {
    public void connected(Connection conn) throws IOException;
}
