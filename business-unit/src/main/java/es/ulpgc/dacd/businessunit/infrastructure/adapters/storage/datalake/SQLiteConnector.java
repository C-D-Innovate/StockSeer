package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datalake;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnector {
    public static Connection connect(String dbUrl) throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}

