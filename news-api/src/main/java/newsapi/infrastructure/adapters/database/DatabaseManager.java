package newsapi.infrastructure.adapters.database;

import newsapi.domain.model.Articles;
import newsapi.infrastructure.ports.database.DatabasePort;

import java.util.List;

public class DatabaseManager implements DatabasePort {
    private final ArticleRepository repository;

    public DatabaseManager(String dbUrl) {
        this.repository = new ArticleRepository(dbUrl);
    }

    public boolean saveArticle(Articles article) {
        return repository.saveArticles(List.of(article));
    }

    public boolean saveArticles(List<Articles> articles) {
        return repository.saveArticles(articles);
    }

    public List<Articles> getAllArticles() {
        return repository.getAllArticles();
    }

    public void close() {
        repository.close();
    }
}
