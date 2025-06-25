package storage.sqlite;

import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.sqlite.DatabaseConnector;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectorTest {

    @Test
    void connect_withValidUrl_shouldReturnConnection() {
        String dbUrl = "jdbc:sqlite::memory:";

        try (Connection connection = DatabaseConnector.connect(dbUrl)) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        } catch (SQLException e) {
            fail("No se esperaba SQLException: " + e.getMessage());
        }
    }

    @Test
    void connect_withInvalidUrl_shouldThrowRuntimeException() {
        String invalidUrl = "jdbc:invalid:url";

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            DatabaseConnector.connect(invalidUrl);
        });

        assertTrue(thrown.getMessage().contains("No se pudo conectar a la base de datos"));
        assertInstanceOf(SQLException.class, thrown.getCause());
    }
}

