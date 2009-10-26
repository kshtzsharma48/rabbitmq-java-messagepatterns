package com.rabbitmq.messagepatterns.unicast.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.messagepatterns.unicast.*;
import com.rabbitmq.utility.ValueOrException;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class ReceiverImpl implements Receiver {
    protected com.rabbitmq.messagepatterns.unicast.Connector connector;
    protected String identity;
    protected String queueName = "";

    protected Channel channel;

    protected QueueingConsumer consumer;
    protected String consumerTag;

    private List<ChannelSetupListener> channelSetupListeners = new ArrayList<ChannelSetupListener>();

    public void addSetupListener(ChannelSetupListener channelSetup) {
        channelSetupListeners.add(channelSetup);
    }

    public void removeSetupListener(ChannelSetupListener channelSetup) {
        channelSetupListeners.remove(channelSetup);
    }

    public String getQueueName() {
        return ("".equals(queueName) ? identity : queueName);
    }

    public void setQueueName(String value) {
        queueName = value;
    }

    public com.rabbitmq.messagepatterns.unicast.Connector getConnector() {
        return connector;
    }

    public void setConnector(com.rabbitmq.messagepatterns.unicast.Connector connector) {
        this.connector = connector;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public ReceiverImpl() {
    }

    public void init() throws Exception {
        checkProps();
        connector.connect(connectionListener);
    }

    protected void checkProps() {
        Validator.checkNotNull(connector, this, "Connector");
        Validator.checkNotNull(queueName, this, "QueueName");
    }

    protected void connect(Connection conn) throws IOException {
        channel = conn.createChannel();
        for (ChannelSetupListener listener : channelSetupListeners) {
            listener.channelSetup(channel);
        }
        consume();
    }

    private ConnectionListener connectionListener = new ConnectionListener() {
        public void connected(Connection conn) throws IOException {
            connect(conn);
        }
    };

    protected void consume() throws IOException {
        consumer = new QueueingConsumer(channel);
        consumerTag = channel.basicConsume(queueName, false, "", consumer);
    }

    protected void cancel() throws IOException {
        channel.basicCancel(consumerTag);
    }

    public ReceivedMessageImpl receive() throws Exception {
        return receive(true);
    }

    public ReceivedMessageImpl receiveNoWait() throws Exception {
        return receive(false);
    }

    private ReceivedMessageImpl receive(final boolean wait) throws Exception {
        final ReceivedMessageImpl[] res = new ReceivedMessageImpl[1];
        while (true) {
            if (connector.attempt(new Thunk() {
                public void run() throws InterruptedException {
                    QueueingConsumer.Delivery del = wait ? consumer.nextDelivery() : consumer.nextDelivery(0);
                    res[0] = del == null ? null : new ReceivedMessageImpl(channel, del);
                }
            }, connectionListener)) break;
        }
        return res[0];
    }

    public void ack(ReceivedMessage m) throws Exception {
        final ReceivedMessageImpl r = (ReceivedMessageImpl) m;
        if (r == null || !r.getChannel().equals(channel)) {
            //must have been reconnected; drop ack since there is
            //no place for it to go
            return;
        }
        //Acks must not be retried since they are tied to the
        //channel on which the message was delivered
        connector.attempt(new Thunk() {
            public void run() throws IOException {
                channel.basicAck(r.getDelivery().getEnvelope().getDeliveryTag(), false);
            }
        }, connectionListener);
    }

}
