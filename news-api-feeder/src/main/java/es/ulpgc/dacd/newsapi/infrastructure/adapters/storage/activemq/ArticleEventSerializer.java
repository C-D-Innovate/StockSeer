package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.activemq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import es.ulpgc.dacd.newsapi.model.ArticleEvent;

import java.time.Instant;

public class ArticleEventSerializer {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class,
                    (JsonSerializer<Instant>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .create();

    public static String toJson(ArticleEvent event) {
        return gson.toJson(event);
    }
}
