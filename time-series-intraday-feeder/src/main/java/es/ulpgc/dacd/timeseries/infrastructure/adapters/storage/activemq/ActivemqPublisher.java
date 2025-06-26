package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.activemq;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.OpeningClosingEventSaver;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.MarketHoursFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class ActivemqPublisher implements OpeningClosingEventSaver {

    private static final Logger logger = Logger.getLogger(ActivemqPublisher.class.getName());

    private final ActiveMQMessageSender messageSender;
    private final LocalDate today;

    public ActivemqPublisher(String brokerUrl, String topic, LocalDate today) {
        this.today = today;

        ActiveMQConnectionManager connection = new ActiveMQConnectionManager(brokerUrl, topic);
        EventJsonSerializer serializer = new EventJsonSerializer();
        this.messageSender = new ActiveMQMessageSender(connection, serializer);
    }

    @Override
    public void saveOpeningAndClosingEvents(List<AlphaVantageEvent> data, String context) {
        List<AlphaVantageEvent> filtered = MarketHoursFilter.filterExactTodayOpeningAndClosing(data, today);

        if (filtered.isEmpty()) {
            logger.info("[ActiveMQ] No se encontraron eventos de apertura ni cierre para publicar.\n");
            return;
        }

        messageSender.send(filtered);
        logger.info("[ActiveMQ] Eventos publicados correctamente\n");
    }
}