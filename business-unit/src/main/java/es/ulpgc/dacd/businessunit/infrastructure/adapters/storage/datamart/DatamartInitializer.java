package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datamart;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatamartInitializer {

    public static void createCleanDatamartTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS clean_datamart (
                symbol TEXT,
                day TEXT,
                open_ts TEXT,
                open_price REAL,
                close_ts TEXT,
                close_price REAL,
                news_count INTEGER,
                avg_sent REAL,
                PRIMARY KEY (symbol, day)
            );
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
