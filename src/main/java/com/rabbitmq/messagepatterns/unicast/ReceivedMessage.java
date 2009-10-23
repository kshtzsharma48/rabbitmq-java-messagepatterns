package com.rabbitmq.messagepatterns.unicast;

public interface ReceivedMessage extends Message {
    public boolean isRedelivered();
}
