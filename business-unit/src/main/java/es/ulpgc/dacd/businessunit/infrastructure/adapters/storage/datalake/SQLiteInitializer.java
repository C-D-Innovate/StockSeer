package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datalake;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteInitializer {
    public static void initializeTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String createNewsTable = """
            CREATE TABLE IF NOT EXISTS dirty_news (
                ts TEXT,
                url TEXT PRIMARY KEY,
                content TEXT,
                fullContent TEXT,
                date TEXT,
                sentiment_label TEXT
            );
            """;

            String createMarketTable = """
            CREATE TABLE IF NOT EXISTS dirty_market (
                symbol TEXT,
                volume INTEGER,
                open_ts TEXT,
                open REAL,
                close_ts TEXT,
                close REAL,
                date TEXT,
                PRIMARY KEY (symbol, open_ts, close_ts)
            );
            """;



            stmt.execute(createNewsTable);
            stmt.execute(createMarketTable);
        }
    }
}