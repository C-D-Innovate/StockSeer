package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    public static Connection connect(String dbUrl) {
        try {
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }
}

