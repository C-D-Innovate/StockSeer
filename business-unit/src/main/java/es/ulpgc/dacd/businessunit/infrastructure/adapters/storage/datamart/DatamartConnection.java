package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datamart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatamartConnection {
    private final String dbUrl;

    public DatamartConnection(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}
