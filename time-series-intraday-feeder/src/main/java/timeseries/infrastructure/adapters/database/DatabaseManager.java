package timeseries.infrastructure.adapters.database;

import timeseries.domain.model.StockData;

import java.sql.*;
import java.util.List;

public class DatabaseManager {

    public static Connection connect(String dbUrl) throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public static void initializeDatabase(String dbUrl) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS stock_data ("
                + "symbol TEXT NOT NULL, "
                + "timestamp TEXT NOT NULL, "
                + "open REAL, "
                + "high REAL, "
                + "low REAL, "
                + "close REAL, "
                + "volume INTEGER, "
                + "PRIMARY KEY (symbol, timestamp)"
                + ")";
        try (Connection conn = connect(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }

    public static void insert(StockData data, String dbUrl) {
        if (!isDuplicateData(data, dbUrl)) {
            String insertSQL = "INSERT OR IGNORE INTO stock_data (symbol, timestamp, open, high, low, close, volume) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = connect(dbUrl);
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, data.getSymbol());
                pstmt.setString(2, data.getTimestamp());
                pstmt.setDouble(3, data.getOpen());
                pstmt.setDouble(4, data.getHigh());
                pstmt.setDouble(5, data.getLow());
                pstmt.setDouble(6, data.getClose());
                pstmt.setLong(7, data.getVolume());
                pstmt.executeUpdate();  // Ejecuta la inserción
            } catch (SQLException e) {
                System.err.println("Error al insertar los datos: " + e.getMessage());
            }
        }
    }

    public static boolean isDuplicateData(StockData data, String dbUrl) {
        String querySQL = "SELECT COUNT(*) FROM stock_data WHERE symbol = ? AND timestamp = ?";
        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setString(1, data.getSymbol());
            pstmt.setString(2, data.getTimestamp());
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error al verificar duplicados: " + e.getMessage());
            return false;
        }
    }

    public static void saveAll(List<StockData> data, String dbUrl) {
        initializeDatabase(dbUrl);
        for (StockData d : data) {
            insert(d, dbUrl);
        }
    }
}
