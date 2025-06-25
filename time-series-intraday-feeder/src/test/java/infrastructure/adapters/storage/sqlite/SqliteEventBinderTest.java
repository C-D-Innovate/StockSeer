package infrastructure.adapters.storage.sqlite;

import es.ulpgc.dacd.timeseries.model.AlphaVantageEvent;
import es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.sqlite.SqliteEventBinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SqliteEventBinderTest {

    @Mock
    private PreparedStatement stmt;

    private SqliteEventBinder binder;
    private AlphaVantageEvent event;

    @BeforeEach
    void setUp() {
        binder = new SqliteEventBinder();
        event = new AlphaVantageEvent(
                "TESTSYM",
                Instant.parse("2025-06-24T12:34:56Z"),
                100.1,
                110.2,
                90.3,
                105.4,
                123456L
        );
    }

    @Test
    void bind_ShouldCallAllSettersWithCorrectParameters() throws Exception {
        binder.bind(stmt, event);

        verify(stmt).setString(1, "TESTSYM");
        verify(stmt).setString(2, "2025-06-24T12:34:56Z");
        verify(stmt).setDouble(3, 100.1);
        verify(stmt).setDouble(4, 110.2);
        verify(stmt).setDouble(5, 90.3);
        verify(stmt).setDouble(6, 105.4);
        verify(stmt).setLong(7, 123456L);

        verifyNoMoreInteractions(stmt);
    }

    @Test
    void bind_WhenSetterThrowsSQLException_ShouldPropagate() throws SQLException {
        lenient().doThrow(new SQLException("db error"))
                .when(stmt).setDouble(4, 110.2);

        assertThrows(SQLException.class, () -> binder.bind(stmt, event));

        verify(stmt).setString(1, "TESTSYM");
        verify(stmt).setString(2, "2025-06-24T12:34:56Z");
        verify(stmt).setDouble(3, 100.1);
        verify(stmt).setDouble(4, 110.2);
    }
}