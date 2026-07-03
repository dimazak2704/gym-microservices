package com.dimazak.workload.listener;

import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterListener {

    private static final String DLQ = "ActiveMQ.DLQ";

    @JmsListener(destination = DLQ)
    public void handleDeadLetter(Message message) {
        log.error("DEAD LETTER received. Message could not be processed after retries. " +
                        "JMS Message ID: {}. Requires manual investigation.",
                extractId(message));
    }

    private String extractId(Message message) {
        try {
            return message.getJMSMessageID();
        } catch (Exception e) {
            return "unknown";
        }
    }
}