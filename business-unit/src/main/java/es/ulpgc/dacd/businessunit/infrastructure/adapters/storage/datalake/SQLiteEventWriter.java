package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datalake;

import es.ulpgc.dacd.businessunit.models.MarketEvent;
import es.ulpgc.dacd.businessunit.models.NewsEvent;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis.Classifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

public class SQLiteEventWriter {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteEventWriter.class);

    private final Connection conn;
    private final Classifier classifier;

    public SQLiteEventWriter(Connection conn, Classifier classifier) {
        this.conn = conn;
        this.classifier = classifier;
    }

    public void saveNewsEvent(NewsEvent event) {
        try {
            String text = Optional.ofNullable(event.fullContent()).orElse(event.content());
            String label = classifier.labelFrom(text);

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

                logger.info("News almacenada con label: {} | URL: {}", label, event.url());
            }
        } catch (Exception e) {
            logger.error("Error guardando NewsEvent: {} -> {}", event.url(), e.getMessage(), e);
        }
    }

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
            logger.info("Market almacenado: {} @ {}", event.symbol(), event.ts());
        } catch (SQLException e) {
            logger.error("Error guardando MarketEvent: {}", e.getMessage(), e);
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
            throw new RuntimeException("Error verificando existencia de URL: " + url, e);
        }
    }

    public boolean containsMarket(String symbol, String ts) {
        String sql = "SELECT 1 FROM dirty_market WHERE symbol = ? AND ts = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, symbol);
            stmt.setString(2, ts);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando existencia de evento de mercado: " + symbol + "@" + ts, e);
        }
    }
}
