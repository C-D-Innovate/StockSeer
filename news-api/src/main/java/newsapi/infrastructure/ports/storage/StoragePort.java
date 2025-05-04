package newsapi.infrastructure.ports.storage;

import newsapi.domain.model.Articles;

import java.util.List;

public interface StoragePort {
    boolean saveArticle(Articles article);
    boolean saveArticles(List<Articles> articles);
    List<Articles> getAllArticles();
    void close();
}

