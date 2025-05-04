package newsapi.infrastructure.adapters.storage;

import newsapi.domain.model.Articles;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleRepository {
    private final Connection connection;

    public ArticleRepository(String dbUrl) {
        this.connection = createConnection(dbUrl);
        createTableIfNotExists();
    }

    public boolean saveArticles(List<Articles> articles) {
        return articles.stream().allMatch(this::saveArticle);
    }

    public boolean saveArticle(Articles article) {
        if (articleExists(article.getUrl())) return false;
        String sql = "INSERT INTO articles(url, publishedAt, content, title) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, article.getUrl());
            stmt.setTimestamp(2, Timestamp.from(article.getPublishedAt()));
            stmt.setString(3, article.getContent());
            stmt.setString(4, article.getTitle());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar artículo: " + e.getMessage());
            return false;
        }
    }

    public List<Articles> getAllArticles() {
        String sql = "SELECT * FROM articles";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<Articles> list = new ArrayList<>();
            while (rs.next()) list.add(toArticle(rs));
            return list;
        } catch (SQLException e) {
            System.err.println("Error al obtener artículos: " + e.getMessage());
            return List.of();
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    private boolean articleExists(String url) {
        String sql = "SELECT 1 FROM articles WHERE url = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, url);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("Error al comprobar existencia: " + e.getMessage());
            return true;
        }
    }

    private Articles toArticle(ResultSet rs) throws SQLException {
        return new Articles(
                rs.getString("url"),
                rs.getTimestamp("publishedAt").toInstant(),
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
