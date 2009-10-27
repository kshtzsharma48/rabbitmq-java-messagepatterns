package com.rabbitmq.messagepatterns.unicast.impl;

import com.rabbitmq.messagepatterns.unicast.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class SenderImpl implements Sender {
    protected Connector connector;
    protected String identity;
    protected String exchangeName = "";
    protected boolean transactional = true;

    protected Channel channel;

    protected long msgIdPrefix;
    protected long msgIdSuffix;

    private List<ChannelSetupListener> channelSetupListeners = new ArrayList<ChannelSetupListener>();
    private List<MessageSentListener> sentListeners = new ArrayList<MessageSentListener>();

    public void addSetupListener(ChannelSetupListener channelSetup) {
        channelSetupListeners.add(channelSetup);
    }

    public void removeSetupListener(ChannelSetupListener channelSetup) {
        channelSetupListeners.remove(channelSetup);
    }

    public void addMessageSentListener(MessageSentListener listener) {
        sentListeners.add(listener);
    }

    public void removeMessageSentListener(MessageSentListener listener) {
        sentListeners.remove(listener);
    }

    public String getCurrentId() {
        return String.format("%16x%16x",
                msgIdPrefix, msgIdSuffix);
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public SenderImpl() {
    }

    public void init() throws Exception {
        init(System.currentTimeMillis());
    }

    public void init(long msgIdPrefix) throws Exception {
        checkProps();
        this.msgIdPrefix = msgIdPrefix;
        msgIdSuffix = 0;

        connector.connect(connectionListener);
    }

    protected void checkProps() {
        Validator.checkNotNull(connector, this, "Connector");
        Validator.checkNotNull(exchangeName, this, "ExchangeName");
    }

    protected void connect(Connection conn) throws IOException {
        channel = conn.createChannel();
        if (transactional) channel.txSelect();

        for (ChannelSetupListener listener : channelSetupListeners) {
            listener.channelSetup(channel);
        }
    }

    private ConnectionListener connectionListener = new ConnectionListener() {
        public void connected(Connection conn) throws IOException {
            connect(conn);
        }
    };

    protected String nextId() {
        String res = getCurrentId();
        msgIdSuffix++;
        return res;
    }

    public MessageImpl createMessage() {
        MessageImpl m = new MessageImpl();
        m.setProperties(new AMQP.BasicProperties());
        m.setFrom(identity);
        m.setMessageId(nextId());
        return m;
    }

    public Message createReply(Message m) {
        MessageImpl r = (MessageImpl) m.createReply();
        r.setMessageId(nextId());
        return r;
    }

    public void send(final Message m) throws Exception {
        while (true) {
            if (connector.attempt(new Thunk() {
                public void run() throws IOException {
                    channel.basicPublish(exchangeName,
                            m.getRoutingKey(),
                            m.getProperties(), m.getBody());
                    if (transactional) channel.txCommit();
                }
            }, connectionListener)) break;
        }
        //TODO: if/when IModel supports 'sent' notifications then
        //we will translate those, rather than firing our own here
        for (MessageSentListener listener : sentListeners) {
            listener.messageSent(m);
        }
    }
}
