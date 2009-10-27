package com.rabbitmq.messagepatterns.unicast;

import java.io.UnsupportedEncodingException;

public class TestHelper {
    public static MessageSentListener sentListener = new MessageSentListener() {
        public void messageSent(Message m) {
            logMessage("sent", m.getFrom(), m);
        }
    };

    public static void logMessage(String action,
                                  String actor,
                                  Message m) {
        System.out.printf("%s %s %s\n", actor, action, decode(m.getBody()));
    }

    public static void logMessage(String action,
                                  Messaging actor,
                                  Message m) {
        logMessage(action, actor.getIdentity(), m);
    }

    public static byte[] encode(String s) {
        try {
            return s.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            assert false;
            return null;
        }
    }

    public static String decode(byte[] b) {
        try {
            return new String(b, "utf-8");
        } catch (UnsupportedEncodingException e) {
            assert false;
            return null;
        }
    }
}
