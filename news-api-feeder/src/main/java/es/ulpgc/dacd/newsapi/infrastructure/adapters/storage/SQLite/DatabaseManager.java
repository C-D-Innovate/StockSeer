package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.SQLite;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.ports.storage.ArticleRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager implements ArticleRepository {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private final Connection connection;
    private final ArticleWriter writer;

    public DatabaseManager(String dbUrl) {
        this.connection = DatabaseConnector.connect(dbUrl);
        DatabaseSchemaInitializer.initialize(connection);
        this.writer = new ArticleWriter(connection);
    }

    @Override
    public boolean saveArticle(ArticleEvent article) {
        return saveArticles(List.of(article)) > 0;
    }

    @Override
    public int saveArticles(List<ArticleEvent> articles) {
        return writer.insertArticles(articles);
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al cerrar conexión: " + e.getMessage(), e);
        }
    }
}

