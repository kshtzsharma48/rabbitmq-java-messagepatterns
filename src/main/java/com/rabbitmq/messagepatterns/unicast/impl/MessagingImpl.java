package com.rabbitmq.messagepatterns.unicast.impl;

import com.rabbitmq.messagepatterns.unicast.*;

class Validator {
    public static void checkNotNull(Object thing, Object c, String prop) {
        if (thing == null) {
            String msg = String.format("'%s' property in %s " +
                    "must not be null",
                    prop, c);
            throw new UnsupportedOperationException(msg);
        }
    }
}

public class MessagingImpl implements com.rabbitmq.messagepatterns.unicast.Messaging {
    protected SenderImpl sender = new SenderImpl();
    protected ReceiverImpl receiver = new ReceiverImpl();

    public Connector getConnector() {
        return sender.getConnector();
    }

    public void setConnector(Connector connector) {
        sender.setConnector(connector);
        receiver.setConnector(connector);
    }

    public String getIdentity() {
        return sender.getIdentity();
    }

    public void setIdentity(String identity) {
        sender.setIdentity(identity);
        receiver.setIdentity(identity);
    }

    public String getExchangeName() {
        return sender.getExchangeName();
    }

    public void setExchangeName(String exchangeName) {
        sender.setExchangeName(exchangeName);
    }

    public boolean isTransactional() {
        return sender.isTransactional();
    }

    public void setTransactional(boolean transactional) {
        sender.setTransactional(transactional);
    }

    public String getCurrentId() {
        return sender.getCurrentId();
    }

    public String getQueueName() {
        return receiver.getQueueName();
    }

    public void setQueueName(String queueName) {
        receiver.setQueueName(queueName);
    }

    public void addSetupListener(ChannelSetupListener channelSetup) {
        receiver.addSetupListener(channelSetup);
        sender.addSetupListener(channelSetup);
    }

    public void removeSetupListener(ChannelSetupListener channelSetup) {
        receiver.removeSetupListener(channelSetup);
        sender.removeSetupListener(channelSetup);
    }

    public void addSenderSetupListener(ChannelSetupListener channelSetup) {
        sender.addSetupListener(channelSetup);
    }

    public void removeSenderSetupListener(ChannelSetupListener channelSetup) {
        sender.removeSetupListener(channelSetup);
    }

    public void addReceiverSetupListener(ChannelSetupListener channelSetup) {
        receiver.addSetupListener(channelSetup);
    }

    public void removeReceiverSetupListener(ChannelSetupListener channelSetup) {
        receiver.removeSetupListener(channelSetup);
    }

    public void addMessageSentListener(MessageSentListener listener) {
        sender.addMessageSentListener(listener);
    }

    public void removeMessageSentListener(MessageSentListener listener) {
        sender.removeMessageSentListener(listener);
    }

    public void init() throws Exception {
        sender.init();
        receiver.init();
    }

    public void init(long msgIdPrefix) throws Exception {
        sender.init(msgIdPrefix);
        receiver.init();
    }

    public Message createMessage() {
        return sender.createMessage();
    }

    public Message createReply(Message m) {
        return sender.createReply(m);
    }

    public void send(Message m) throws Exception {
        sender.send(m);
    }

    public ReceivedMessage receive() throws Exception {
        return receiver.receive();
    }

    public ReceivedMessage receiveNoWait() throws Exception {
        return receiver.receiveNoWait();
    }

    public void ack(ReceivedMessage m) throws Exception {
        receiver.ack(m);
    }

    public MessagingImpl() {
    }
}