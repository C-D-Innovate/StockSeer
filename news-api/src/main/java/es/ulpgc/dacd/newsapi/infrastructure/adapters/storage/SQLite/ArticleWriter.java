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
        INSERT INTO articles (url, publishedAt, title, fullContent)
        VALUES (?, ?, ?, ?)
    """;
        int count = 0;
        for (ArticleEvent article : articles) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                String fallbackContent = article.getFullContent();
                if (fallbackContent == null || fallbackContent.isBlank()) {
                    fallbackContent = article.getContent();
                }

                stmt.setString(1, article.getUrl());
                stmt.setString(2, article.getPublishedAt().toString());
                stmt.setString(3, article.getTitle());
                stmt.setString(4, fallbackContent);

                stmt.executeUpdate();
                count++;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al guardar artículo: ", e);
            }
        }
        return count;
    }


}
