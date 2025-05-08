package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.StockDataStorage;

import java.sql.*;
import java.util.List;

public class SqliteManager implements StockDataStorage {

    private final String dbUrl;

    public SqliteManager(String dbUrl) {
        this.dbUrl = dbUrl;
        initializeDatabase();
    }

    @Override
    public void saveOpeningAndClosingEvents(List<AlphaVantageEvent> data, String context) {
        List<AlphaVantageEvent> filteredEvents = MarketHoursFilter.filterExactTodayOpeningAndClosing(data);

        if (filteredEvents.isEmpty()) {
            System.out.println("No hay eventos de apertura o cierre para guardar.");

            if (!data.isEmpty()) {
                AlphaVantageEvent latest = data.getFirst();
                System.out.println("Último evento recibido de la API:");
                System.out.println(latest);
            }
            return;
        }

        insertEvents(filteredEvents);
    }

    private void insertEvents(List<AlphaVantageEvent> events) {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            try (PreparedStatement stmt = prepareInsertStatement(connection)) {
                for (AlphaVantageEvent event : events) {
                    bindEventToStatement(stmt, event);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                System.out.println("→ Guardados en SQLite: " + events.size() + " evento(s)");
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar eventos en SQLite: " + e.getMessage());
        }
    }

    private PreparedStatement prepareInsertStatement(Connection connection) throws SQLException {
        String sql = """
                INSERT OR IGNORE INTO events (symbol, ts, open, high, low, close, volume)
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;
        return connection.prepareStatement(sql);
    }

    private void bindEventToStatement(PreparedStatement stmt, AlphaVantageEvent event) throws SQLException {
        stmt.setString(1, event.getSymbol());
        stmt.setString(2, event.getTs().toString());
        stmt.setDouble(3, event.getOpen());
        stmt.setDouble(4, event.getHigh());
        stmt.setDouble(5, event.getLow());
        stmt.setDouble(6, event.getClose());
        stmt.setLong(7, event.getVolume());
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = """
                    CREATE TABLE IF NOT EXISTS events (
                        symbol TEXT NOT NULL,
                        ts TEXT NOT NULL,
                        open REAL,
                        high REAL,
                        low REAL,
                        close REAL,
                        volume INTEGER,
                        PRIMARY KEY (symbol, ts)
                    );
                    """;
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
}
