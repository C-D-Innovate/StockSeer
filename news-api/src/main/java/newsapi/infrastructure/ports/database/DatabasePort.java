package newsapi.infrastructure.ports.database;

import newsapi.domain.model.Articles;

import java.util.List;

public interface DatabasePort {
    boolean saveArticle(Articles article);
    boolean saveArticles(List<Articles> articles);
    List<Articles> getAllArticles();
    void close();
}

