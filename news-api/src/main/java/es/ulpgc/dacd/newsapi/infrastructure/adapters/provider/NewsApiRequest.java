package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.models.request.EverythingRequest;

import java.time.Instant;

public class NewsApiRequest {
    private final String language;

    public NewsApiRequest(String language) {
        this.language = language;
    }

    public EverythingRequest build(String query, String from, String to) {
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
                date += "Z";
            }
            return Instant.parse(date).toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha inválido: " + date, e);
        }
    }

}
