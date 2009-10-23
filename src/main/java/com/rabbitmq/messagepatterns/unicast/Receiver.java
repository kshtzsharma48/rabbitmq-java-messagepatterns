package com.rabbitmq.messagepatterns.unicast;

public interface Receiver extends MessagingCommon {
    public void addSetupListener(ChannelSetupListener channelSetup);
    public void removeSetupListener(ChannelSetupListener channelSetup);

    public String getQueueName();
    public void setQueueName(String name);

    public void init() throws Exception;

    public ReceivedMessage receive() throws Exception;

    public ReceivedMessage receiveNoWait() throws Exception;

    public void ack(ReceivedMessage m) throws Exception;
}
