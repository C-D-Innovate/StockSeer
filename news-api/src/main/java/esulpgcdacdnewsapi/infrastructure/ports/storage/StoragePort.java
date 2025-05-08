package esulpgcdacdnewsapi.infrastructure.ports.storage;

import esulpgcdacdnewsapi.domain.model.ArticleEvent;

import java.util.List;

public interface StoragePort {
    int saveArticles(List<ArticleEvent> articles); // ← antes devolvía boolean
    boolean saveArticle(ArticleEvent article);
    List<ArticleEvent> getAllArticles();
    void close();
}


