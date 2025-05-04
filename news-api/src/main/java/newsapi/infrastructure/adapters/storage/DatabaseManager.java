package newsapi.infrastructure.adapters.storage;

import newsapi.domain.model.Articles;
import newsapi.infrastructure.ports.storage.StoragePort;

import java.util.List;

public class DatabaseManager implements StoragePort {
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
