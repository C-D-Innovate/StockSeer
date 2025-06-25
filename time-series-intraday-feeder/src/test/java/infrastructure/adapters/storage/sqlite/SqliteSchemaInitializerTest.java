package infrastructure.adapters.storage.sqlite;

import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.sqlite.SqliteSchemaInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class SqliteSchemaInitializerTest {

    @BeforeAll
    static void enableByteBuddyExperimental() {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Test
    void initialize_SuccessfulExecutesSqlAndClosesConnection() throws Exception {
        String dbUrl = "jdbc:sqlite:dummy-db-url";

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            Connection connMock = mock(Connection.class);
            Statement stmtMock = mock(Statement.class);

            dm.when(() -> DriverManager.getConnection(dbUrl))
                    .thenReturn(connMock);
            when(connMock.createStatement()).thenReturn(stmtMock);

            new SqliteSchemaInitializer(dbUrl).initialize();

            verify(connMock).createStatement();
            verify(stmtMock).execute(argThat(sql ->
                    sql.contains("CREATE TABLE IF NOT EXISTS events")
            ));
            verify(connMock).close();
        }
    }

    @Test
    void initialize_WhenGetConnectionThrows_ShouldWrapInRuntimeException() {
        String dbUrl = "jdbc:sqlite:invalid";
        SQLException sqlException = new SQLException("connection error");

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(dbUrl))
                    .thenThrow(sqlException);

            SqliteSchemaInitializer initializer = new SqliteSchemaInitializer(dbUrl);
            RuntimeException ex = assertThrows(RuntimeException.class, initializer::initialize);
            assertTrue(ex.getMessage().contains("Error al inicializar la base de datos SQLite"));
            assertSame(sqlException, ex.getCause(), "Debe propagar la misma SQLException stubbed");
        }
    }

    @Test
    void initialize_WhenExecuteThrows_ShouldWrapInRuntimeException() throws Exception {
        String dbUrl = "jdbc:sqlite:another-db-url";

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            Connection connMock = mock(Connection.class);
            Statement stmtMock = mock(Statement.class);

            dm.when(() -> DriverManager.getConnection(dbUrl))
                    .thenReturn(connMock);
            when(connMock.createStatement()).thenReturn(stmtMock);
            doThrow(new SQLException("execute error")).when(stmtMock).execute(anyString());

            SqliteSchemaInitializer initializer = new SqliteSchemaInitializer(dbUrl);
            RuntimeException ex = assertThrows(RuntimeException.class, initializer::initialize);
            assertTrue(ex.getMessage().contains("Error al inicializar la base de datos SQLite"));
            assertTrue(ex.getCause() instanceof SQLException);
            verify(connMock).close();
        }
    }
}