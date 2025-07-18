package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datamart;

import es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalanalysis.RatioCalculator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatamartWriter {

    private final RatioCalculator asignarRatio;

    public DatamartWriter() {
        this.asignarRatio = new RatioCalculator();
    }

    public void merge(Connection conn) throws SQLException {
        try (var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM clean_datamart");
        }

        String insertSql = """
        INSERT INTO clean_datamart (symbol, day, open_ts, open_price, close_ts, close_price, news_count, avg_sent)
        SELECT
            m.symbol,
            DATE(m.open_ts) AS day,
            m.open_ts,
            m.open,
            m.close_ts,
            m.close,
            COUNT(DISTINCT n.url) AS news_count,
            NULL AS avg_sent
        FROM dirty_market m
        INNER JOIN dirty_news n ON DATE(m.open_ts) = DATE(n.ts)
        GROUP BY m.symbol, DATE(m.open_ts), m.open_ts, m.close_ts, m.open, m.close;
        """;



        try (var stmt = conn.createStatement()) {
            stmt.execute(insertSql);
        }

        updateAvgSentiment(conn);
    }

    public void updateAvgSentiment(Connection conn) throws SQLException {

        String query = """
            SELECT DATE(ts) AS day, GROUP_CONCAT(sentiment_label) AS sentiments
            FROM dirty_news
            GROUP BY DATE(ts);
            """;


        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String day = rs.getString("day");
                String sentimentsStr = rs.getString("sentiments");
                double avgSent = asignarRatio.calculateRatio(sentimentsStr.split(","));

                String updateSql = "UPDATE clean_datamart SET avg_sent = ? WHERE day = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setDouble(1, avgSent);
                    updatePs.setString(2, day);
                    updatePs.executeUpdate();
                }
            }
        }
    }
}
