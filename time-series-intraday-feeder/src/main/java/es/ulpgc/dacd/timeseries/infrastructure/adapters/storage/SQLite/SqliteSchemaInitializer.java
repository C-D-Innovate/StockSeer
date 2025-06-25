package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.SQLite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteSchemaInitializer {

    private final String dbUrl;

    public SqliteSchemaInitializer(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void initialize() {
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

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar la base de datos SQLite", e);
        }
    }
}