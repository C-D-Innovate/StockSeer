package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.ActiveMQ;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.OpeningClosingEventSaver;
import es.ulpgc.dacd.timeseries.infrastructure.utils.EventJsonSerializer;
import es.ulpgc.dacd.timeseries.infrastructure.utils.MarketHoursFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class ActivemqPublisher implements OpeningClosingEventSaver {

    private static final Logger logger = Logger.getLogger(ActivemqPublisher.class.getName());

    private final ActiveMQMessageSender messageSender;
    private final LocalDate today;

    public ActivemqPublisher(String brokerUrl, String topicName, LocalDate today) {
        this.today = today;

        ActiveMQConnectionManager connection = new ActiveMQConnectionManager(brokerUrl, topicName);
        EventJsonSerializer serializer = new EventJsonSerializer();
        this.messageSender = new ActiveMQMessageSender(connection, serializer);
    }

    @Override
    public void saveOpeningAndClosingEvents(List<AlphaVantageEvent> data, String context) {
        List<AlphaVantageEvent> filtered = MarketHoursFilter.filterExactTodayOpeningAndClosing(data, today);

        if (filtered.isEmpty()) {
            logger.info("[ActiveMQ] No se encontraron eventos de apertura ni cierre para publicar.");
            return;
        }

        messageSender.send(filtered);
        logger.info("[ActiveMQ] Eventos publicados correctamente");
    }
}