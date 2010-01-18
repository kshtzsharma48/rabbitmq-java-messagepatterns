package com.rabbitmq.messagepatterns.unicast.impl;

import com.rabbitmq.client.Channel;

import java.io.Closeable;
import java.io.IOException;

public abstract class ChannelAware implements Closeable {

    protected Channel channel;

    public void close() throws IOException {
        channel.close();
    }

}
