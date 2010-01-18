package com.rabbitmq.messagepatterns.unicast.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.messagepatterns.unicast.Disposable;

public abstract class ChannelAware implements Disposable {

    protected Channel channel;

    public void terminate() throws Exception {
        channel.close();
    }

}
