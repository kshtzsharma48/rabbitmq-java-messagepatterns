package com.rabbitmq.messagepatterns.unicast;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.AMQP;

public interface Message {
    public AMQP.BasicProperties getProperties();
    public void setProperties(AMQP.BasicProperties properties);

    public byte[] getBody();
    public void setBody(byte[] body);

    public String getRoutingKey();
    public void setRoutingKey(String routingKey);

    public String getFrom();
    public void setFrom(String from);

    public String getTo();
    public void setTo(String to);

    public String getReplyTo();
    public void setReplyTo(String replyTo);

    public String getMessageId();
    public void setMessageId(String messageId);

    public String getCorrelationId();
    public void setCorrelationId(String correlationId);

    public Message createReply();
}

