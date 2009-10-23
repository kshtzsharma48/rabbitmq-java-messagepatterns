package com.rabbitmq.messagepatterns.unicast;

public interface MessageSentListener {
    public void messageSent(Message m);
}
