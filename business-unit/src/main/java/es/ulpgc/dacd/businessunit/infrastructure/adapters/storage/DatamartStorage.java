package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage;

import java.sql.*;

public class DatamartStorage {
    private final String dbUrl;

    public DatamartStorage(String dbUrl) {
        this.dbUrl = dbUrl;
        createCleanTable();
    }

    private void createCleanTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS clean_datamart (
                symbol TEXT,
                day TEXT,
                open_ts TEXT,
                close_ts TEXT,
                open_price REAL,
                close_price REAL,
                news_count INTEGER,
                avg_sent REAL,
                PRIMARY KEY (symbol, day)
            );
            """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error creando clean_datamart: " + e.getMessage(), e);
        }
    }

    public void mergeToDatamart() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM clean_datamart");

            String insertSql = """
                INSERT INTO clean_datamart (
                    symbol, day, open_ts, close_ts, open_price, close_price, news_count, avg_sent
                )
                SELECT
                    m.symbol,
                    m.date AS day,
                    MIN(CASE strftime('%H:%M', m.ts) WHEN '13:30' THEN m.ts ELSE NULL END) AS open_ts,
                    MIN(CASE strftime('%H:%M', m.ts) WHEN '20:00' THEN m.ts ELSE NULL END) AS close_ts,
                    MIN(CASE strftime('%H:%M', m.ts) WHEN '13:30' THEN m.price ELSE NULL END) AS open_price,
                    MIN(CASE strftime('%H:%M', m.ts) WHEN '20:00' THEN m.price ELSE NULL END) AS close_price,
                    COUNT(DISTINCT n.url) AS news_count,
                    NULL AS avg_sent
                FROM dirty_market m
                LEFT JOIN dirty_news n ON m.date = n.date
                GROUP BY m.symbol, m.date;
                """;

            stmt.execute(insertSql);

        } catch (SQLException e) {
            System.err.println("❌ Error al consolidar clean_datamart: " + e.getMessage());
        }
    }

    public void resetTempTables() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM dirty_market");
            stmt.execute("DELETE FROM dirty_news");
        } catch (SQLException e) {
            System.err.println("❌ Error limpiando tablas temporales: " + e.getMessage());
        }
    }
}
