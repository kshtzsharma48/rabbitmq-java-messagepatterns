#!/bin/sh
mvn -Dmdep.outputFile=/tmp/cp.txt dependency:build-classpath > /dev/null
java -cp target/test-classes:target/classes:`cat /tmp/cp.txt` com.rabbitmq.messagepatterns.unicast.UnicastClient $@

