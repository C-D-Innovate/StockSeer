package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.models.request.EverythingRequest;

import java.time.Instant;

public class NewsApiRequestBuilder {
    private final String language;

    public NewsApiRequestBuilder(String language) {
        this.language = language;
    }

    public EverythingRequest build(String query, String from, String to) {
        // Convertir a Instant para asegurarnos de que el formato es válido
        from = validateDate(from);
        to = validateDate(to);

        return new EverythingRequest.Builder()
                .q(query)
                .language(language)
                .from(from)
                .to(to)
                .build();
    }

    private String validateDate(String date) {
        try {
            if (!date.endsWith("Z")) {
                date += "Z"; // Añadir 'Z' si falta
            }
            return Instant.parse(date).toString(); // Asegura formato ISO 8601 con UTC
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha inválido: " + date, e);
        }
    }

}
