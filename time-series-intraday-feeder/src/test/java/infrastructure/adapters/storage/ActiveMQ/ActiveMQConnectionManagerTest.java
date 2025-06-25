package infrastructure.adapters.storage.ActiveMQ;

import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.activemq.ActiveMQConnectionManager;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import javax.jms.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActiveMQConnectionManagerTest {

    @Test
    void constructor_WhenAllJmsCallsSucceed_ShouldSetUpConnectionSessionTopicAndProducer() throws Exception {
        Connection connectionMock = mock(Connection.class);
        Session sessionMock = mock(Session.class);
        Topic topicMock = mock(Topic.class);
        MessageProducer producerMock = mock(MessageProducer.class);

        try (MockedConstruction<ActiveMQConnectionFactory> mocked = mockConstruction(
                ActiveMQConnectionFactory.class,
                (factoryMock, context) -> {
                    when(factoryMock.createConnection()).thenReturn(connectionMock);
                    when(connectionMock.createSession(false, Session.AUTO_ACKNOWLEDGE))
                            .thenReturn(sessionMock);
                    when(sessionMock.createTopic("myTopic")).thenReturn(topicMock);
                    when(sessionMock.createProducer(topicMock)).thenReturn(producerMock);
                }
        )) {
            ActiveMQConnectionManager mgr =
                    new ActiveMQConnectionManager("tcp://fake-broker:61616", "myTopic");

            assertEquals(1, mocked.constructed().size());

            ActiveMQConnectionFactory factoryMock = mocked.constructed().get(0);
            verify(factoryMock).createConnection();
            verify(connectionMock).start();

            verify(connectionMock).createSession(false, Session.AUTO_ACKNOWLEDGE);
            verify(sessionMock).createTopic("myTopic");
            verify(sessionMock).createProducer(topicMock);

            assertSame(sessionMock, mgr.session());
            assertSame(topicMock,   mgr.topic());
            assertSame(producerMock, mgr.producer());
        }
    }

    @Test
    void constructor_WhenCreateConnectionThrows_ShouldWrapInRuntimeException() {
        try (MockedConstruction<ActiveMQConnectionFactory> mocked = mockConstruction(
                ActiveMQConnectionFactory.class,
                (factoryMock, context) -> {
                    when(factoryMock.createConnection())
                            .thenThrow(new JMSException("no broker"));
                }
        )) {
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    new ActiveMQConnectionManager("tcp://bad-broker", "topic")
            );
            assertTrue(ex.getMessage().contains("No se pudo inicializar la conexión con ActiveMQ"));
            assertTrue(ex.getCause() instanceof JMSException);
        }
    }
}