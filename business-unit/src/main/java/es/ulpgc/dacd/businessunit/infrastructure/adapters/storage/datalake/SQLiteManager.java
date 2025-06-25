package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datalake;

import es.ulpgc.dacd.businessunit.models.MarketEvent;
import es.ulpgc.dacd.businessunit.models.NewsEvent;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis.CalculateLabel;
import es.ulpgc.dacd.businessunit.infrastructure.ports.EventStorage;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteManager implements EventStorage, AutoCloseable {
    private final Connection conn;
    private final SQLiteEventWriter writer;

    public SQLiteManager(String dbUrl, CalculateLabel labelCalculator) {
        try {
            this.conn = SQLiteConnector.connect(dbUrl);
            SQLiteInitializer.initializeTables(conn);
            this.writer = new SQLiteEventWriter(conn, labelCalculator);
        } catch (SQLException e) {
            throw new RuntimeException("Error conectando o inicializando la base de datos", e);
        }
    }

    @Override
    public void saveMarketEvent(MarketEvent event) {
        writer.saveMarketEvent(event);
    }

    @Override
    public void saveNewsEvent(NewsEvent event) {
        writer.saveNewsEvent(event);
    }

    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Override
    public boolean containsUrl(String url) {
        return writer.containsUrl(url);
    }

    @Override
    public boolean containsMarket(String symbol, String ts) {
        return writer.containsMarket(symbol, ts);
    }




}
