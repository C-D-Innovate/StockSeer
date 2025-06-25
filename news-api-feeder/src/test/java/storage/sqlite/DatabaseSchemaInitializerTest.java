package storage.sqlite;

import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.sqlite.DatabaseSchemaInitializer;
import org.junit.jupiter.api.*;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseSchemaInitializerTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void initialize_createsArticlesTable() throws SQLException {
        DatabaseSchemaInitializer.initialize(connection);

        try (ResultSet rs = connection.getMetaData().getTables(null, null, "articles", null)) {
            assertTrue(rs.next(), "La tabla 'articles' debería existir tras la inicialización.");
        }

        try (ResultSet rs = connection.getMetaData().getColumns(null, null, "articles", null)) {
            assertTrue(rs.next(), "La tabla 'articles' debería tener al menos una columna.");
            assertEquals("url", rs.getString("COLUMN_NAME"));
            assertTrue(rs.next());
            assertEquals("publishedAt", rs.getString("COLUMN_NAME"));
            assertTrue(rs.next());
            assertEquals("title", rs.getString("COLUMN_NAME"));
            assertTrue(rs.next());
            assertEquals("fullContent", rs.getString("COLUMN_NAME"));
        }
    }

    @Test
    void initialize_doesNotThrowIfTableAlreadyExists() {
        assertDoesNotThrow(() -> {
            DatabaseSchemaInitializer.initialize(connection);
            DatabaseSchemaInitializer.initialize(connection);
        });
    }
}

