package com.rabbitmq.messagepatterns.unicast;

import java.io.Closeable;

public interface Sender extends Closeable, MessagingCommon {
    /**
     * Event issued when a connection requires setup. This will be fired upon initial connection, and
     * then whenever a failure requires the connection to be re-established.
     */
    public void addSetupListener(ChannelSetupListener channelSetup);
    public void removeSetupListener(ChannelSetupListener channelSetup);
    public void addMessageSentListener(MessageSentListener listener);
    public void removeMessageSentListener(MessageSentListener listener);

    public String getExchangeName();
    public void setExchangeName(String name);

    public boolean isTransactional();
    public void setTransactional(boolean transactional);

    public String getCurrentId();

    void init() throws Exception;
    void init(long msgIdPrefix) throws Exception;

    Message createMessage();
    Message createReply(Message m);
    void send(Message m) throws Exception;
}
