package com.rabbitmq.messagepatterns.unicast.impl;

import com.rabbitmq.messagepatterns.unicast.*;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.impl.AMQImpl;

import java.io.IOException;
import java.io.EOFException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

public class ConnectorImpl implements com.rabbitmq.messagepatterns.unicast.Connector
{
    protected int pause = 1000; // ms
    protected int attempts = 60;

    protected ConnectorState state = ConnectorState.DISCONNECTED;
    protected ConnectionBuilder builder;
    protected Connection connection;
    protected Semaphore closed = new Semaphore(1);

    public int getPause() {
        return pause;
    }

    public void setPause(int pause) {
        this.pause = pause;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public ConnectionBuilder getBuilder() {
        return builder;
    }

    public ConnectorState getState() {
        return state;
    }

    private List<ConnectorStateListener> listeners = new ArrayList<ConnectorStateListener>();

    public void addConnectorStateListener(ConnectorStateListener listener) {
        listeners.add(listener);
    }

    public void removeConnectorStateListener(ConnectorStateListener listener) {
        listeners.remove(listener);
    }

    public ConnectorImpl(ConnectionBuilder builder)
    {
        this.builder = builder;
    }

    public void connect(final ConnectionListener d) throws Exception {
        final Connection conn = connect();
        Exception e = attempt(new Thunk () {
            public void run() throws IOException {
                d.connected(conn);
            }
        });
        if (e == null) return;
        if (!reconnect(d)) throw e;		// TODO: This exception should probably be wrapped to preserve the initial stack trace
    }

    public boolean attempt(Thunk t, ConnectionListener d) throws Exception {
        Exception e = attempt(t);
        if (e == null) return true;
        if (!reconnect(d)) throw e;		// TODO: This exception should probably be wrapped to preserve the initial stack trace
        return false;
    }

    public void close()
    {
        closed.release();
        synchronized (this)
        {
            if (connection != null) connection.abort();
        }
    }

    // TODO port this?
    /*void IDisposable.Dispose()
    {
        close();
    }*/

    protected synchronized Connection connect() throws Exception
    {
        if (connection != null)
        {
            ShutdownSignalException closeReason = connection.getCloseReason();
            if (closeReason == null) return connection;
            if (!isShutdownRecoverable(closeReason))
                throw closeReason;
        }
        onStateChange(ConnectorState.RECONNECTING);
        Exception e = null;
        for (int i = 0; i < attempts; i++)
        {
            e = attempt(new Thunk() {
                public void run() throws IOException {
                    connection = builder.createConnection();
                }
            });
            onStateChange(ConnectorState.CONNECTED);
            if (e == null) return connection;
            if (closed.tryAcquire(pause, TimeUnit.MILLISECONDS)) break;
        }
        throw (e);
    }

    protected boolean reconnect(final ConnectionListener d) throws Exception
    {
        try
        {
            while (true)
            {
                final Connection conn = connect();
                Exception e = attempt(new Thunk() {
                    public void run() throws IOException {
                        d.connected(conn);
                    }
                });
                if (e == null) return true;
            }
        } catch (Exception e) {
            // Ignore
        }

        onStateChange(ConnectorState.DISCONNECTED);
        return false;
    }


    protected static boolean isShutdownRecoverable(ShutdownSignalException s)
    {
        if (s != null) {
            int replyCode = 0;
            if (s.getReason() instanceof AMQImpl.Connection.Close) {
                replyCode = ((AMQImpl.Connection.Close) s.getReason()).getReplyCode();
            }

            return s.isInitiatedByApplication() &&
                    ((replyCode == AMQP.CONNECTION_FORCED) ||
                     (replyCode == AMQP.INTERNAL_ERROR) ||
                     (s.getCause() instanceof EOFException));
        }
        return false;
    }

    protected static Exception attempt(Thunk t) throws Exception {
        try
        {
            t.run();
            return null;
        }
        catch (AlreadyClosedException e)
        {
            if (isShutdownRecoverable(e))
            {
                return e;
            }
            else
            {
                throw e;
            }
        }
/*			catch (OperationInterruptedException e)
        {
            if (isShutdownRecoverable(e.ShutdownReason))
            {
                return e;
            }
            else
            {
                throw e;
            }
        }
        catch (BrokerUnreachableException e)
        {
            //TODO: we may want to be more specific here
            return e;
        }*/
        catch (IOException e)
        {
            //TODO: we may want to be more specific here
            return e;
        }
    }

    protected void onStateChange(ConnectorState newState) {
        state = newState;

        for (ConnectorStateListener listener : listeners) {
            listener.connectorStateChanged(this, state);
        }
    }
}