package com.dimazak.workload.listener;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadLetterListenerTest {

    @InjectMocks
    private DeadLetterListener listener;

    @Test
    void handleDeadLetter_shouldNotThrow() throws JMSException {
        Message message = mock(Message.class);
        when(message.getJMSMessageID()).thenReturn("ID:test-123");

        assertDoesNotThrow(() -> listener.handleDeadLetter(message));
    }

    @Test
    void handleDeadLetter_shouldHandleExceptionInGetMessageId() throws JMSException {
        Message message = mock(Message.class);
        when(message.getJMSMessageID()).thenThrow(new JMSException("error"));

        assertDoesNotThrow(() -> listener.handleDeadLetter(message));
    }
}