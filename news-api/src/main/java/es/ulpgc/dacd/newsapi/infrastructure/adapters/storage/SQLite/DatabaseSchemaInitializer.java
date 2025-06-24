package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.SQLite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSchemaInitializer {
    public static void initialize(Connection connection) {
        String sql = """
            CREATE TABLE IF NOT EXISTS articles (
                url TEXT PRIMARY KEY,
                publishedAt TEXT,
                content TEXT,
                title TEXT,
                fullContent TEXT
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo crear la tabla", e);
        }
    }
}

