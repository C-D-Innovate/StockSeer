package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.SQLite;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.utils.EventJsonSerializer;

import java.sql.*;
import java.util.List;
import java.util.logging.Logger;

public class SqliteEventSaver {

    private static final Logger logger = Logger.getLogger(SqliteEventSaver.class.getName());
    private final String dbUrl;
    private final SqliteEventBinder binder;
    private final EventJsonSerializer serializer;

    public SqliteEventSaver(String dbUrl) {
        this.dbUrl = dbUrl;
        this.binder = new SqliteEventBinder();
        this.serializer = new EventJsonSerializer();
    }

    public void save(List<AlphaVantageEvent> events) {
        try (Connection connection = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = connection.prepareStatement(
                     "INSERT OR IGNORE INTO events (symbol, ts, open, high, low, close, volume) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            for (AlphaVantageEvent event : events) {
                logger.info("Insertando evento: " + serializer.serialize(event));  // 👈 log JSON
                binder.bind(stmt, event);
                stmt.addBatch();
            }

            stmt.executeBatch();
            logger.info("→ Guardados en SQLite: " + events.size() + " evento(s)");

        } catch (SQLException e) {
            logger.severe("Error al guardar eventos en SQLite: " + e.getMessage());
        }
    }
}
