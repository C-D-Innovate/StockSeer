package timeseries.infrastructure;

import timeseries.domain.model.StockData;

import java.sql.*;

public class DatabaseManager {

    public static Connection connect(String dbUrl) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC no encontrado");
        }
        return DriverManager.getConnection(dbUrl);
    }

    public static void initializeDatabase(String dbUrl) {
        String sql = """
            CREATE TABLE IF NOT EXISTS stock_data (
                symbol TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                open REAL,
                high REAL,
                low REAL,
                close REAL,
                volume INTEGER,
                PRIMARY KEY (symbol, timestamp)
            );
        """;
        try (Connection conn = connect(dbUrl); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    public static void insert(StockData data, String dbUrl) {
        String insertSQL = """
            INSERT OR IGNORE INTO stock_data (symbol, timestamp, open, high, low, close, volume)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = connect(dbUrl); PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setString(1, data.getSymbol());
            stmt.setString(2, data.getTimestamp());
            stmt.setDouble(3, data.getOpen());
            stmt.setDouble(4, data.getHigh());
            stmt.setDouble(5, data.getLow());
            stmt.setDouble(6, data.getClose());
            stmt.setLong(7, data.getVolume());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al insertar en BD: " + e.getMessage());
        }
    }

    public static boolean isDuplicateData(StockData data, String dbUrl) {
        String sql = """
        SELECT COUNT(*) FROM stock_data
        WHERE symbol = ?
        AND open = ?
        AND high = ?
        AND low = ?
        AND close = ?
        AND volume = ?
        """;

        try (Connection conn = connect(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data.getSymbol());
            stmt.setDouble(2, data.getOpen());
            stmt.setDouble(3, data.getHigh());
            stmt.setDouble(4, data.getLow());
            stmt.setDouble(5, data.getClose());
            stmt.setLong(6, data.getVolume());

            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("Error comprobando duplicado: " + e.getMessage());
            return false;
        }
    }
}
