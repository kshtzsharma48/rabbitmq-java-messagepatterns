package com.rabbitmq.messagepatterns.unicast;
//NB: For testing we declare all resources as auto-delete and
//non-durable, to avoid manual cleanup. More typically the
//resources would be non-auto-delete/non-exclusive and durable, so
//that they survives server and client restarts.

import com.rabbitmq.client.*;

import java.io.IOException;
import java.io.EOFException;

import junit.framework.TestCase;

public class UnicastTest extends TestCase {
    private ConnectionFactory _factory;
    private ConnectionBuilder _builder;
        MessagingClosure senderSetup = new MessagingClosure() {
            public ChannelSetupListener create(final Messaging m) {
                return new ChannelSetupListener() {
                    public void channelSetup(Channel channel) throws IOException {
                        declareExchange(channel, m.getExchangeName(), "fanout");
                    }
                };
            }
        };

    public UnicastTest() {
        _factory = new ConnectionFactory();
        _builder = new ConnectionBuilder() {
            public Connection createConnection() throws IOException {
                return _factory.newConnection("localhost");
            }
        };
    }

    protected void declareExchange(Channel m, String name, String type) throws IOException {
        m.exchangeDeclare(name, type, false, false, true, null);
    }

    protected void declareQueue(Channel m, String name) throws IOException {
        m.queueDeclare(name, false, false, false, true, null);
    }

    protected void bindQueue(Channel m, String q, String x, String rk) throws IOException {
        m.queueBind(q, x, rk);
    }

    public void testDirect() throws Exception {
        Connector conn = Factory.createConnector(_builder);
        try {
            //create two parties
            final Messaging foo = Factory.createMessaging();
            foo.setConnector(conn);
            foo.setIdentity("foo");
            foo.addMessageSentListener(TestHelper.sentListener);
            foo.addReceiverSetupListener(new ChannelSetupListener() {
                public void channelSetup(Channel channel) throws IOException {
                    declareQueue(channel, foo.getIdentity());
                }
            });
            foo.init();

            final Messaging bar = Factory.createMessaging();
            bar.setConnector(conn);
            bar.setIdentity("bar");
            bar.addMessageSentListener(TestHelper.sentListener);
            bar.addReceiverSetupListener(new ChannelSetupListener() {
                public void channelSetup(Channel channel) throws IOException {
                    declareQueue(channel, bar.getIdentity());
                }
            });
            bar.init();

            //send message from foo to bar
            Message mf = foo.createMessage();
            mf.setBody(TestHelper.encode("message1"));
            mf.setTo("bar");
            foo.send(mf);

            //receive message at bar and reply
            ReceivedMessage rb = bar.receive();
            TestHelper.logMessage("recv", bar, rb);
            Message mb = bar.createReply(rb);
            mb.setBody(TestHelper.encode("message2"));
            bar.send(mb);
            bar.ack(rb);

            //receive reply at foo
            ReceivedMessage rf = foo.receive();
            TestHelper.logMessage("recv", foo, rf);
            foo.ack(rf);
        } finally {
            conn.close();
        }
    }

    private interface MessagingClosure {
        public ChannelSetupListener create(Messaging m);
    }

    public void testRelayed() throws Exception {
        MessagingClosure senderSetup = new MessagingClosure() {
            public ChannelSetupListener create(final Messaging m) {
                return new ChannelSetupListener() {
                    public void channelSetup(Channel channel) throws IOException {
                        declareExchange(channel, m.getExchangeName(), "fanout");
                    }
                };
            }
        };

        MessagingClosure receiverSetup = new MessagingClosure() {
            public ChannelSetupListener create(final Messaging m) {
                return new ChannelSetupListener() {
                    public void channelSetup(Channel channel) throws IOException {
                        declareExchange(channel, "out", "direct");
                        declareQueue(channel, m.getQueueName());
                        bindQueue(channel, m.getQueueName(), "out", m.getQueueName());
                    }
                };
            }
        };

        testRelayedHelper(senderSetup, receiverSetup, _builder);
    }

    protected void testRelayedHelper(MessagingClosure senderSetup,
                                     MessagingClosure receiverSetup,
                                     ConnectionBuilder builder) throws Exception {
        Connector conn = Factory.createConnector(builder);
        Connector relayConn = Factory.createConnector(builder);
        try {
            //create relay
            final Messaging relay = Factory.createMessaging();
            relay.setConnector(relayConn);
            relay.setIdentity("relay");
            relay.setExchangeName("out");
            relay.addSenderSetupListener(new ChannelSetupListener() {
                public void channelSetup(Channel channel) throws IOException {
                    declareExchange(channel, relay.getExchangeName(), "direct");
                }
            });
            relay.addReceiverSetupListener(new ChannelSetupListener() {
                public void channelSetup(Channel channel) throws IOException {
                    declareExchange(channel, "in", "fanout");
                    declareQueue(channel, relay.getQueueName());
                    bindQueue(channel, relay.getQueueName(), "in", "");
                }
            });
            relay.addMessageSentListener(TestHelper.sentListener);

            relay.init();

            //create relay
            new Thread() {
                public void run() {
                    //receive messages and pass it on
                    ReceivedMessage r;
                    while (true) {
                        try {
                            r = relay.receive();
                            TestHelper.logMessage("recv", relay, r);
                            relay.send(r);
                            relay.ack(r);
                        }
                        catch (EOFException e) {
                        }
                        catch (AlreadyClosedException e) {
                        }
                        catch (InterruptedException e) {
                        }
                        catch (ShutdownSignalException e) {
                        }
                        catch (Exception e) {
                            System.out.println("**** EXCEPTION IN RELAY THREAD ****");
                            System.out.println(e);
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                }
            }.start();

            //create two parties
            Messaging foo = Factory.createMessaging();
            foo.setConnector(conn);
            foo.setIdentity("foo");
            foo.addSenderSetupListener(senderSetup.create(foo));
            foo.addReceiverSetupListener(receiverSetup.create(foo));
            foo.setExchangeName("in");
            foo.addMessageSentListener(TestHelper.sentListener);
            foo.init();

            Messaging bar = Factory.createMessaging();
            bar.setConnector(conn);
            bar.setIdentity("bar");
            bar.addSenderSetupListener(senderSetup.create(bar));
            bar.addReceiverSetupListener(receiverSetup.create(bar));
            bar.setExchangeName("in");
            bar.addMessageSentListener(TestHelper.sentListener);
            bar.init();

            //send message from foo to bar
            Message mf = foo.createMessage();
            mf.setBody(TestHelper.encode("message1"));
            mf.setTo("bar");
            foo.send(mf);

            //receive message at bar and reply
            ReceivedMessage rb = bar.receive();
            TestHelper.logMessage("recv", bar, rb);
            Message mb = bar.createReply(rb);
            mb.setBody(TestHelper.encode("message2"));
            bar.send(mb);
            bar.ack(rb);

            //receive reply at foo
            ReceivedMessage rf = foo.receive();
            TestHelper.logMessage("recv", foo, rf);
            foo.ack(rf);
        }
        finally {
            conn.close();
            relayConn.close();
        }
    }


    public void testPreconfigured() throws Exception {
        Thread.sleep(500);
        
        //The idea here is to simulate a setup where are the
        //resources are pre-declared outside the Unicast messaging
        //framework.
        Connection conn = _builder.createConnection();
        try {
            Channel ch = conn.createChannel();

            //declare exchanges
            declareExchange(ch, "in", "fanout");
            declareExchange(ch, "out", "direct");

            //declare queue and binding for relay
            declareQueue(ch, "relay");
            bindQueue(ch, "relay", "in", "");

            //declare queue and binding for two participants
            declareQueue(ch, "foo");
            bindQueue(ch, "foo", "out", "foo");
            declareQueue(ch, "bar");
            bindQueue(ch, "bar", "out", "bar");

            //set up participants, send some messages
            MessagingClosure dummy = new MessagingClosure() {
                public ChannelSetupListener create(final Messaging m) {
                    return new ChannelSetupListener() {
                        public void channelSetup(Channel channel) throws IOException {
                        }
                    };
                }
            };

            testRelayedHelper(dummy, dummy, _builder);
        }
        finally {
            conn.close();
        }
    }

}
