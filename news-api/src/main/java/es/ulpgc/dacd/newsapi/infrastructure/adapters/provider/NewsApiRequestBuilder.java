package es.ulpgc.dacd.newsapi.infrastructure.adapters.provider;

import com.kwabenaberko.newsapilib.models.request.EverythingRequest;

public class NewsApiRequestBuilder {
    private final String language;

    public NewsApiRequestBuilder(String language) {
        this.language = language;
    }

    public EverythingRequest build(String query, String from, String to) {
        return new EverythingRequest.Builder()
                .q(query)
                .language(language)
                .from(from)
                .to(to)
                .build();
    }
}
