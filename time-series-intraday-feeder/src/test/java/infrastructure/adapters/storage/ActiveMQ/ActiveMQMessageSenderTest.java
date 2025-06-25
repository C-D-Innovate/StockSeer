package infrastructure.adapters.storage.ActiveMQ;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.activemq.ActiveMQConnectionManager;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.activemq.ActiveMQMessageSender;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.activemq.EventJsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveMQMessageSenderTest {

    @Mock
    private ActiveMQConnectionManager connection;

    @Mock
    private Session session;

    @Mock
    private Topic topic;

    @Mock
    private MessageProducer producer;

    @Mock
    private EventJsonSerializer serializer;

    private ActiveMQMessageSender sender;

    @BeforeEach
    void setUp() {
        when(connection.session()).thenReturn(session);
        when(connection.topic()).thenReturn(topic);
        when(connection.producer()).thenReturn(producer);
        sender = new ActiveMQMessageSender(connection, serializer);
    }

    @Test
    void send_SuccessfulEvents_ShouldSerializeAndSendEach() throws JMSException {
        AlphaVantageEvent event1 = mock(AlphaVantageEvent.class);
        AlphaVantageEvent event2 = mock(AlphaVantageEvent.class);

        when(serializer.serialize(event1)).thenReturn("{\"foo\":1}");
        when(serializer.serialize(event2)).thenReturn("{\"bar\":2}");

        TextMessage msg1 = mock(TextMessage.class);
        TextMessage msg2 = mock(TextMessage.class);
        when(session.createTextMessage("{\"foo\":1}")).thenReturn(msg1);
        when(session.createTextMessage("{\"bar\":2}")).thenReturn(msg2);

        sender.send(List.of(event1, event2));

        verify(serializer).serialize(event1);
        verify(serializer).serialize(event2);
        verify(session).createTextMessage("{\"foo\":1}");
        verify(session).createTextMessage("{\"bar\":2}");
        verify(producer).send(topic, msg1);
        verify(producer).send(topic, msg2);
    }

    @Test
    void send_WhenCreateTextMessageThrows_ShouldCatchAndNotThrow() throws JMSException {
        AlphaVantageEvent event = mock(AlphaVantageEvent.class);
        when(serializer.serialize(event)).thenReturn("json");
        when(session.createTextMessage("json")).thenThrow(new JMSException("fail"));

        assertDoesNotThrow(() -> sender.send(List.of(event)));

        verify(session).createTextMessage("json");
        verifyNoInteractions(producer);
    }

    @Test
    void send_WhenProducerSendThrows_ShouldCatchAndContinue() throws JMSException {
        AlphaVantageEvent event = mock(AlphaVantageEvent.class);
        when(serializer.serialize(event)).thenReturn("json2");
        TextMessage msg = mock(TextMessage.class);
        when(session.createTextMessage("json2")).thenReturn(msg);
        doThrow(new JMSException("send fail")).when(producer).send(topic, msg);

        assertDoesNotThrow(() -> sender.send(List.of(event)));

        verify(session).createTextMessage("json2");
        verify(producer).send(topic, msg);
    }
}