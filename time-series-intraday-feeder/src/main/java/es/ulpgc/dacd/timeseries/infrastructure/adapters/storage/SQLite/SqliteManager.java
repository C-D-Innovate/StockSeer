package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.sqlite;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.OpeningClosingEventSaver;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.MarketHoursFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class SqliteManager implements OpeningClosingEventSaver {

    private static final Logger logger = Logger.getLogger(SqliteManager.class.getName());
    private final SqliteEventSaver eventSaver;
    private final LocalDate today;

    public SqliteManager(String dbUrl, LocalDate today) {
        new SqliteSchemaInitializer(dbUrl).initialize();
        this.eventSaver = new SqliteEventSaver(dbUrl);
        this.today = today;
    }

    @Override
    public void saveOpeningAndClosingEvents(List<AlphaVantageEvent> data, String context) {
        List<AlphaVantageEvent> filtered = MarketHoursFilter.filterExactTodayOpeningAndClosing(data, today);

        if (filtered.isEmpty()) {
            logger.info("No hay eventos de apertura o cierre para guardar.");
            if (!data.isEmpty()) {
                logger.info("Último evento recibido de la API:\n" + data.getFirst());
            }
            return;
        }

        eventSaver.save(filtered);
    }
}