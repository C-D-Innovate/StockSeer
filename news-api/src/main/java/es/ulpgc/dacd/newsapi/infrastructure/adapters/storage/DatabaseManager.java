package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.StoragePort;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager implements StoragePort {
    private final Connection connection;

    public DatabaseManager(String dbUrl) {
        this.connection = connect(dbUrl);
        initializeSchema();
    }

    @Override
    public boolean saveArticle(ArticleEvent article) {
        return saveArticles(List.of(article)) > 0;
    }

    @Override
    public int saveArticles(List<ArticleEvent> articles) {
        String sql = """
            INSERT INTO articles
                (url, publishedAt, content, title, fullContent)
            VALUES (?, ?, ?, ?, ?)
        """;  // ← Añadido fullContent
        int successCount = 0;

        for (ArticleEvent article : articles) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, article.getUrl());
                stmt.setString(2, article.getPublishedAt().toString());
                stmt.setString(3, article.getContent());
                stmt.setString(4, article.getTitle());
                stmt.setString(5, article.getFullContent());  // ← SET del nuevo campo
                stmt.executeUpdate();
                successCount++;
            } catch (SQLException e) {
                System.err.println("Error al guardar artículo: " + e.getMessage());
            }
        }

        return successCount;
    }

    @Override
    public List<ArticleEvent> getAllArticles() {
        List<ArticleEvent> result = new ArrayList<>();
        String sql = "SELECT * FROM articles";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener artículos: " + e.getMessage());
        }

        return result;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    private Connection connect(String dbUrl) {
        try {
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }

    private void initializeSchema() {
        String sql = """
            CREATE TABLE IF NOT EXISTS articles (
                url TEXT PRIMARY KEY,
                publishedAt TEXT,
                content TEXT,
                title TEXT,
                fullContent TEXT    -- ← Nueva columna
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo crear la tabla", e);
        }
    }

    private ArticleEvent fromResultSet(ResultSet rs) throws SQLException {
        return new ArticleEvent(
                rs.getString("url"),
                Instant.parse(rs.getString("publishedAt")),
                rs.getString("content"),
                rs.getString("title"),
                rs.getString("fullContent")   // ← Recuperamos fullContent
        );
    }
}