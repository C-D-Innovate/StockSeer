package es.ulpgc.dacd.newsapi.infrastructure.ports.storage;

import es.ulpgc.dacd.newsapi.model.ArticleEvent;

import java.util.List;

public interface ArticleSaver {
    int saveArticles(List<ArticleEvent> articles);
    boolean saveArticle(ArticleEvent article);
    void close();
}


