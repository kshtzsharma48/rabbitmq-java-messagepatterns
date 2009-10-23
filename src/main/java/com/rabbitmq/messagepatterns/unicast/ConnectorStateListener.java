package com.rabbitmq.messagepatterns.unicast;

/**
 Interface that can be provided to the messaging infrastructure to receive notifications
 about endpoint state.
 <param name="sender">the connector that is sending the state</param>
 <param name="state">the new state the connector has entered</param>
 */
public interface ConnectorStateListener {
    public void connectorStateChanged(Connector sender, ConnectorState state);
}
