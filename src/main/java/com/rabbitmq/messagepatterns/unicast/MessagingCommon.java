package com.rabbitmq.messagepatterns.unicast;

public interface MessagingCommon {
    public Connector getConnector();
    public void setConnector(Connector connector);

    public String getIdentity();
    public void setIdentity(String connector);
}
