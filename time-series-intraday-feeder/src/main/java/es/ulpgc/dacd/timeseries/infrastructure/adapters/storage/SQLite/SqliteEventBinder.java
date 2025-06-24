package es.ulpgc.dacd.timeseries.infrastructure.adapters.storage.SQLite;

import es.ulpgc.dacd.timeseries.domain.model.AlphaVantageEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqliteEventBinder {

    public void bind(PreparedStatement stmt, AlphaVantageEvent event) throws SQLException {
        stmt.setString(1, event.getSymbol());
        stmt.setString(2, event.getTs().toString());
        stmt.setDouble(3, event.getOpen());
        stmt.setDouble(4, event.getHigh());
        stmt.setDouble(5, event.getLow());
        stmt.setDouble(6, event.getClose());
        stmt.setLong(7, event.getVolume());
    }
}