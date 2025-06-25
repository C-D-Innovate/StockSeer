package es.ulpgc.dacd.newsapi.infrastructure.ports.storage;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;

import java.util.List;

public interface ArticleRepository {
    int saveArticles(List<ArticleEvent> articles);
    boolean saveArticle(ArticleEvent article);
    void close();
}


