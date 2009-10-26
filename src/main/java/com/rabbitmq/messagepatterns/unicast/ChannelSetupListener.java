package com.rabbitmq.messagepatterns.unicast;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public interface ChannelSetupListener {
    public void channelSetup(Channel channel) throws IOException;
}
