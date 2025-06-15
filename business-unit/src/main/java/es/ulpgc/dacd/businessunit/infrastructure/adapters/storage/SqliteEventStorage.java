package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage;

import es.ulpgc.dacd.businessunit.domain.model.MarketEvent;
import es.ulpgc.dacd.businessunit.domain.model.NewsEvent;
import es.ulpgc.dacd.businessunit.infrastructure.ports.EventStorage;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.utils.PythonScriptRunner;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

public class SqliteEventStorage implements EventStorage, AutoCloseable {

    private final Connection conn;
    private final PythonScriptRunner pythonRunner;

    public SqliteEventStorage(String dbUrl, PythonScriptRunner pythonRunner) {
        try {
            this.conn = DriverManager.getConnection(dbUrl);
            this.pythonRunner = pythonRunner;
            initializeTables();
        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar o inicializar la base de datos: " + e.getMessage(), e);
        }
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            String createNewsTable = """
                CREATE TABLE IF NOT EXISTS dirty_news (
                    ts TEXT,
                    url TEXT PRIMARY KEY,
                    content TEXT,
                    fullContent TEXT,
                    date TEXT,
                    sentiment_label TEXT
                );
                """;

            String createMarketTable = """
                CREATE TABLE IF NOT EXISTS dirty_market (
                    ts TEXT,
                    symbol TEXT,
                    price REAL,
                    volume INTEGER,
                    date TEXT,
                    PRIMARY KEY (symbol, ts)
                );
                """;

            stmt.execute(createNewsTable);
            stmt.execute(createMarketTable);
        }
    }

    @Override
    public void saveMarketEvent(MarketEvent event) {
        String sql = "INSERT OR IGNORE INTO dirty_market (ts, symbol, price, volume, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String date = LocalDate.ofInstant(event.ts(), ZoneOffset.UTC).toString();

            pstmt.setString(1, event.ts().toString());
            pstmt.setString(2, event.symbol());
            pstmt.setDouble(3, event.price());
            pstmt.setLong(4, event.volume());
            pstmt.setString(5, date);

            pstmt.executeUpdate();
            System.out.println("Evento NewsEvent almacenado en tiempo real: " + event.symbol() + " @ " + event.ts());
        } catch (SQLException e) {
            System.err.println("Error guardando MarketEvent: " + e.getMessage());
        }
    }

    @Override
    public void saveNewsEvent(NewsEvent event) {
        System.out.println("Procesando NewsEvent: " + event.url());
        try {
            // Ejecutar script Python para análisis de sentimiento
            String textToAnalyze = Optional.ofNullable(event.fullContent()).orElse(event.content());
            String label = pythonRunner.runAnalysisScript(textToAnalyze);

            String sql = """
                INSERT OR IGNORE INTO dirty_news (ts, url, content, fullContent, date, sentiment_label) 
                VALUES (?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String date = LocalDate.ofInstant(event.ts(), ZoneOffset.UTC).toString();

                pstmt.setString(1, event.ts().toString());
                pstmt.setString(2, event.url());
                pstmt.setString(3, event.content());
                pstmt.setString(4, event.fullContent());
                pstmt.setString(5, date);
                pstmt.setString(6, label);

                pstmt.executeUpdate();
                System.out.println("Guardado en DB con etiqueta: " + label);
                System.out.println("Evento NewsEvent almacenado en tiempo real: " + event.url() + " @ " + event.ts());
            }
        } catch (Exception e) {
            System.err.println("Error guardando NewsEvent con URL: " + event.url() + " -> " + e.getMessage());
            e.printStackTrace();
        }

    }

    public boolean containsUrl(String url) {
        String sql = "SELECT 1 FROM dirty_news WHERE url = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, url);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error comprobando existencia de URL: " + url, e);
        }
    }


    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
