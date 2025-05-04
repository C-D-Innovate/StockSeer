package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.ports.storage.StockDataStorage;

import java.sql.*;
import java.util.List;

public class SqliteManager implements StockDataStorage {

    private final String dbUrl;
    private AlphaVantageEvent lastSaved;

    public SqliteManager(String dbUrl) {
        this.dbUrl = dbUrl;
        initializeDatabase();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    @Override
    public void saveAll(List<AlphaVantageEvent> data, String dbUrl) {
        data.forEach(this::insert);
    }

    @Override
    public boolean isDuplicate(AlphaVantageEvent data, String dbUrl) {
        return lastSaved != null && lastSaved.getSymbol().equals(data.getSymbol()) &&
                lastSaved.getTimestamp().equals(data.getTimestamp());
    }

    public void insert(AlphaVantageEvent data) {
        if (!isDuplicate(data, dbUrl)) {
            String insertSQL = "INSERT OR IGNORE INTO stock_data (ss, symbol, timestamp, open, high, low, close, volume) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, data.getSs());
                pstmt.setString(2, data.getSymbol());
                pstmt.setString(3, data.getFormattedTimestamp());
                pstmt.setDouble(4, data.getOpen());
                pstmt.setDouble(5, data.getHigh());
                pstmt.setDouble(6, data.getLow());
                pstmt.setDouble(7, data.getClose());
                pstmt.setLong(8, data.getVolume());
                pstmt.executeUpdate();
                lastSaved = data;
            } catch (SQLException e) {
                System.err.println("Error al insertar los datos: " + e.getMessage());
            }
        }
    }

    private void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS stock_data (" +
                "ss TEXT NOT NULL, " +
                "symbol TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL, " +
                "open REAL, " +
                "high REAL, " +
                "low REAL, " +
                "close REAL, " +
                "volume INTEGER, " +
                "PRIMARY KEY (symbol, timestamp)" +
                ")";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
}