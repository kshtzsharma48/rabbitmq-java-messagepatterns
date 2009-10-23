package com.rabbitmq.messagepatterns.unicast;

import com.rabbitmq.messagepatterns.unicast.impl.ConnectorImpl;
import com.rabbitmq.messagepatterns.unicast.impl.MessagingImpl;
import com.rabbitmq.messagepatterns.unicast.impl.SenderImpl;
import com.rabbitmq.messagepatterns.unicast.impl.ReceiverImpl;

public class Factory {
    public static Connector createConnector(ConnectionBuilder builder) {
        return new ConnectorImpl(builder);
    }

    public static Sender createSender() {
        return new SenderImpl();
    }

    public static Receiver createReceiver() {
        return new ReceiverImpl();
    }

    public static Messaging createMessaging() {
        return new MessagingImpl();
    }
}
