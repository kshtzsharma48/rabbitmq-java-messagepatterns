package com.rabbitmq.messagepatterns.unicast;

import com.rabbitmq.client.Channel;

public interface ChannelSetupListener {
    public void channelSetup(Channel channel);
}
