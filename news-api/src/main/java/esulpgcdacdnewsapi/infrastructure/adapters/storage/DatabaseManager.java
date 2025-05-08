package esulpgcdacdnewsapi.infrastructure.adapters.storage;

import esulpgcdacdnewsapi.domain.model.ArticleEvent;
import esulpgcdacdnewsapi.infrastructure.ports.storage.StoragePort;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager implements StoragePort {
    private final Connection connection;

    public DatabaseManager(String dbUrl) {
        this.connection = createConnection(dbUrl);
        createTableIfNotExists();
    }

    @Override
    public boolean saveArticle(ArticleEvent article) {
        return saveArticles(List.of(article)) > 0;
    }


    @Override
    public int saveArticles(List<ArticleEvent> articles) {
        String sql = "INSERT INTO articles(url, publishedAt, content, title) VALUES (?, ?, ?, ?)";
        int successCount = 0;

        for (ArticleEvent article : articles) {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, article.getUrl());
                stmt.setString(2, article.getPublishedAt().toString());
                stmt.setString(3, article.getContent());
                stmt.setString(4, article.getTitle());
                stmt.executeUpdate();
                successCount++;
            } catch (SQLException e) {
                // Error por duplicado o fallo, no se incrementa el contador
                System.err.println("Error al guardar artículo: " + e.getMessage());
            }
        }

        return successCount;
    }


    @Override
    public List<ArticleEvent> getAllArticles() {
        String sql = "SELECT * FROM articles";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<ArticleEvent> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRowToArticle(rs));
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error al obtener artículos: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    private ArticleEvent mapRowToArticle(ResultSet rs) throws SQLException {
        return new ArticleEvent(
                rs.getString("url"),
                Instant.parse(rs.getString("publishedAt")),
                rs.getString("content"),
                rs.getString("title")
        );
    }

    private Connection createConnection(String dbUrl) {
        try {
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS articles (
                url TEXT PRIMARY KEY,
                publishedAt TEXT,
                content TEXT,
                title TEXT
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo crear la tabla", e);
        }
    }
}
