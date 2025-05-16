package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage;

import es.ulpgc.dacd.businessunit.domain.model.MarketEvent;
import es.ulpgc.dacd.businessunit.domain.model.NewsEvent;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.ports.out.EventStorage;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class SqliteEventStorage implements EventStorage {
    private final String dbUrl;

    public SqliteEventStorage(String dbUrl) {
        this.dbUrl = dbUrl;
        initializeTables();
    }

    private void initializeTables() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {

            String createNewsTable = """
                CREATE TABLE IF NOT EXISTS dirty_news (
                    ts TEXT,
                    url TEXT PRIMARY KEY,
                    full_content TEXT,
                    date TEXT
                );
                """;

            String createMarketTable = """
                CREATE TABLE IF NOT EXISTS dirty_market (
                    ts TEXT,
                    symbol TEXT,
                    price REAL,
                    volume INTEGER,
                    date TEXT,
                    PRIMARY KEY (symbol, ts)
                );
                """;

            stmt.execute(createNewsTable);
            stmt.execute(createMarketTable);
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error al inicializar tablas: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveMarketEvent(MarketEvent event) {
        String sql = "INSERT OR REPLACE INTO dirty_market (ts, symbol, price, volume, date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String date = LocalDate.ofInstant(event.ts(), ZoneOffset.UTC).toString();

            pstmt.setString(1, event.ts().toString());
            pstmt.setString(2, event.symbol());
            pstmt.setDouble(3, event.price());
            pstmt.setLong(4, event.volume());
            pstmt.setString(5, date);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Error guardando MarketEvent: " + e.getMessage());
        }
    }

    @Override
    public void saveNewsEvent(NewsEvent event) {
        String sql = "INSERT OR REPLACE INTO dirty_news (ts, url, full_content, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String date = LocalDate.ofInstant(event.ts(), ZoneOffset.UTC).toString();

            pstmt.setString(1, event.ts().toString());
            pstmt.setString(2, event.url());
            pstmt.setString(3, event.fullContent());
            pstmt.setString(4, date);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Error guardando NewsEvent: " + e.getMessage());
        }
    }
}
