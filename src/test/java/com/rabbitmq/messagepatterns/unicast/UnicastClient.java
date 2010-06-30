package com.rabbitmq.messagepatterns.unicast;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.util.HashSet;
import java.util.Set;
import java.io.IOException;

public class UnicastClient {

    public static void main(String[] args) throws Exception {
        UnicastClient c = new UnicastClient();
        c.run(args[0], args[1], Integer.parseInt(args[2]));
    }

    Set pending = new HashSet();

    int sent; //requests sent
    int recv; //requests received
    int pend; //pending requests
    int disc; //replies discarded

    UnicastClient() {
    }

    MessageSentListener listener = new MessageSentListener() {
        public void messageSent(Message msg) {
            if (msg.getCorrelationId() == null) {
                sent++;
                pend++;
            }
            displayStats();
        }
    };

    void displayStats() {
        System.out.printf("\r" +
                "sent %d, " +
                "recv %d, " +
                "pend %d, " +
                "disc %d",
                sent, recv, pend, disc);
    }

    void run(final String me, final String you, int sleep) throws Exception {
        Connector conn =
                Factory.createConnector(new ConnectionBuilder() {
                    public Connection createConnection() throws IOException {
                        return new ConnectionFactory().newConnection();
                    }
                });
        Messaging m = Factory.createMessaging();
        m.setConnector(conn);
        m.setIdentity(me);
        m.addMessageSentListener(listener);
        m.addSenderSetupListener(new ChannelSetupListener() {
            public void channelSetup(Channel channel) throws IOException {
                //We declare the recipient queue here to avoid
                //sending messages into the ether. That's an ok
                //thing to do for testing
                channel.queueDeclare(you, true, false, false, null); //durable
            }
        });
        m.addReceiverSetupListener(new ChannelSetupListener() {
            public void channelSetup(Channel channel) throws IOException {
                channel.queueDeclare(me, true, false, false, null); //durable
            }
        });
        m.init();
        byte[] body = new byte[0];
        for (int i = 0; ; i++) {
            //send
            Message msg = m.createMessage();
            msg.setBody(body);
            msg.setTo(you);
            msg.getProperties().setDeliveryMode(2);
            m.send(msg);
            pending.add(msg.getMessageId());
            //handle inbound
            while (true) {
                ReceivedMessage r = m.receiveNoWait();
                if (r == null) break;
                if (r.getCorrelationId() == null) {
                    recv++;
                    displayStats();
                    m.send(m.createReply(r));
                } else {
                    if (pending.contains(r.getCorrelationId())) {
                        pending.remove(r.getCorrelationId());
                        pend--;
                    } else {
                        disc++;
                    }
                    displayStats();
                }
                m.ack(r);
            }
            //Slow down to prevent pending from growing w/o bounds
            Thread.sleep(sleep + pend);
        }
    }
}

