package com.rabbitmq.messagepatterns.unicast;

/**
 * Underlying interface provided by services that support executing reliable operations
 * on a channel.
 */
public interface Connector {
    /**
     * Event issued when the state of the messaging handler changes.
     */
    public void addConnectorStateListener(ConnectorStateListener listener);
    public void removeConnectorStateListener(ConnectorStateListener listener);

    /**
     * Retrieves the state of the connector.
     */
    public ConnectorState getState();

    /**
     * The number of milliseconds to pause between reconnection attempts.
     */
    public int getPause();
    public void setPause(int millis);

    /**
     * The number of reconnection attempts to make in a row before entering a disconnected state.
     */
    public int getAttempts();
    public void setAttempts(int attempts);

    /**
     * Requests that the connector open a connection, and invoke the given delegate with the connection
     * details.
     * <param name="d">the delegate to inform of the connection</param>
     */
    public void connect(ConnectionListener d) throws Exception;

    /**
     * Requests that the connector attempt the given operation. If the connection fails whilst attempting
     * to perform this operation, the connection delegate will be informed of any newly created connections.
     * <param name="t">the thunk to attempt</param>
     * <param name="d">the connection delegate to inform of any new connections</param>
     * <returns>true - the operation succeeded; false - it did not</returns>
     */
    public boolean attempt(Thunk t, ConnectionListener d) throws Exception;

    /**
     * Closes the connector and frees up any underlying resources it is consuming.
     */
    void close();
}
