package com.rabbitmq.messagepatterns.unicast.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.messagepatterns.unicast.Message;
import com.rabbitmq.messagepatterns.unicast.ReceivedMessage;

class MessageImpl implements Message {
    protected AMQP.BasicProperties properties;
    protected byte[] body;
    protected String routingKey;

    public AMQP.BasicProperties getProperties() {
        return properties;
    }

    public void setProperties(AMQP.BasicProperties properties) {
        this.properties = properties;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getFrom() {
        return properties.getUserId();
    }

    public void setFrom(String from) {
        properties.setUserId(from);
    }

    public String getTo() {
        return routingKey;
    }

    public void setTo(String to) {
        routingKey = to;
    }

    public String getReplyTo() {
        return properties.getReplyTo();
    }

    public void setReplyTo(String replyTo) {
        properties.setReplyTo(replyTo);
    }

    public String getMessageId() {
        return properties.getMessageId();
    }

    public void setMessageId(String messageId) {
        properties.setMessageId(messageId);
    }

    public String getCorrelationId() {
        return properties.getCorrelationId();
    }

    public void setCorrelationId(String CorrelationId) {
        properties.setCorrelationId(CorrelationId);
    }

    public MessageImpl() {
    }

    public MessageImpl(AMQP.BasicProperties props, byte[] body, String rk) {
        properties = props;
        this.body = body;
        routingKey = rk;
    }

    public MessageImpl createReply() {
        MessageImpl m = new MessageImpl(properties, // TODO port this! .clone(),
                body,
                routingKey);

        m.setFrom(getTo());
        m.setTo(getReplyTo() == null ? getFrom() : getReplyTo());
        m.setReplyTo(null);
        m.setCorrelationId(getMessageId());
        m.setMessageId(null);

        return m;
    }

}

class ReceivedMessageImpl extends MessageImpl implements ReceivedMessage {

    protected Channel channel;
    protected QueueingConsumer.Delivery delivery;

    public boolean isRedelivered() {
        return delivery.getEnvelope().isRedeliver();
    }

    public Channel getChannel() {
        return channel;
    }

    public QueueingConsumer.Delivery getDelivery(){
        return delivery;
    }

    public ReceivedMessageImpl(Channel channel,  QueueingConsumer.Delivery delivery) {
        super(delivery.getProperties(),
             delivery.getBody(),
             delivery.getEnvelope().getRoutingKey());
        this.channel = channel;
        this.delivery = delivery;
    }
}