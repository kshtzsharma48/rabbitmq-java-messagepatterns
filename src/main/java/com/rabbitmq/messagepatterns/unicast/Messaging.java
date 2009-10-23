package com.rabbitmq.messagepatterns.unicast;

public interface Messaging extends Sender, Receiver {
    public void addSenderSetupListener(ChannelSetupListener channelSetup);
    public void removeSenderSetupListener(ChannelSetupListener channelSetup);
    public void addReceiverSetupListener(ChannelSetupListener channelSetup);
    public void removeReceiverSetupListener(ChannelSetupListener channelSetup);

    void init() throws Exception;
    void init(long msgIdPrefix) throws Exception;
}
