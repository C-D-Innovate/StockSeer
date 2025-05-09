package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.models.Article;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleMapper {
    private static final Logger LOGGER = Logger.getLogger(ArticleMapper.class.getName());
    private final String sourceSystem;

    public ArticleMapper(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public ArticleEvent map(Article article, String topic, String to) {
        try {
            Instant publishedAt = parseDate(article.getPublishedAt()); // Usamos el método seguro
            Instant ts = parseDate(to);  // Usamos el método seguro

            return new ArticleEvent(
                    topic,
                    sourceSystem,
                    ts,
                    article.getUrl(),
                    publishedAt,
                    article.getContent(),
                    article.getTitle()
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error mapping article: " + article.getUrl(), e);
            return null;
        }
    }

    public Instant parseDate(String dateString) {
        try {
            // Recortar milisegundos a un máximo de 3 decimales
            if (dateString.contains(".")) {
                int index = dateString.indexOf(".");
                int endIndex = dateString.indexOf("Z") > 0 ? dateString.indexOf("Z") : dateString.length();
                dateString = dateString.substring(0, Math.min(index + 4, endIndex)); // Limitar a 3 decimales
            }

            // Asegurar que la fecha termina en 'Z' para que sea UTC
            if (!dateString.endsWith("Z")) {
                dateString += "Z";
            }

            return Instant.parse(dateString);
        } catch (DateTimeParseException e) {
            LOGGER.warning("Fecha con formato inválido: " + dateString + ". Se usará el momento actual como fallback.");
            return Instant.now();
        }
    }



}
