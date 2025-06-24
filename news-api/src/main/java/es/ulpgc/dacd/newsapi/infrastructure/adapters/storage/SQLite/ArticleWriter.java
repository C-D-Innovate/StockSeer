package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.SQLite;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleWriter {
    private static final Logger LOGGER = Logger.getLogger(ArticleWriter.class.getName());
    private final Connection connection;

    public ArticleWriter(Connection connection) {
        this.connection = connection;
    }

    public int insertArticles(List<ArticleEvent> articles) {
        String sql = """
            INSERT INTO articles (url, publishedAt, content, title, fullContent)
            VALUES (?, ?, ?, ?, ?)
        """;
        int count = 0;
        for (ArticleEvent article : articles) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, article.getUrl());
                stmt.setString(2, article.getPublishedAt().toString());
                stmt.setString(3, article.getContent());
                stmt.setString(4, article.getTitle());
                stmt.setString(5, article.getFullContent());
                stmt.executeUpdate();
                count++;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al guardar artículo: " + e.getMessage(), e);
            }
        }
        return count;
    }

    public List<ArticleEvent> fetchAllArticles() {
        List<ArticleEvent> result = new ArrayList<>();
        String sql = "SELECT * FROM articles";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(fromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener artículos: " + e.getMessage(), e);
        }

        return result;
    }

    private ArticleEvent fromResultSet(ResultSet rs) throws SQLException {
        return new ArticleEvent(
                null,
                null,
                null,
                rs.getString("url"),
                Instant.parse(rs.getString("publishedAt")),
                rs.getString("content"),
                rs.getString("title"),
                rs.getString("fullContent")
        );
    }

}
