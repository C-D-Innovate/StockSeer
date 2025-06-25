package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.ActiveMQ.ArticleEventSerializer;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ArticleEventSerializerTest {

    @Test
    void toJson_shouldSerializeAllFieldsCorrectly() {
        ArticleEvent event = new ArticleEvent(
                "news-topic",
                "source-service",
                Instant.parse("2025-06-25T10:00:00Z"),
                "https://example.com/article1",
                Instant.parse("2025-06-25T09:00:00Z"),
                "Short summary content",
                "Breaking News",
                "Full content of the article..."
        );

        String json = ArticleEventSerializer.toJson(event);

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        assertEquals("news-topic", jsonObject.get("topic").getAsString());
        assertEquals("source-service", jsonObject.get("ss").getAsString());
        assertEquals("2025-06-25T10:00:00Z", jsonObject.get("ts").getAsString());
        assertEquals("https://example.com/article1", jsonObject.get("url").getAsString());
        assertEquals("2025-06-25T09:00:00Z", jsonObject.get("publishedAt").getAsString());
        assertEquals("Short summary content", jsonObject.get("content").getAsString());
        assertEquals("Breaking News", jsonObject.get("title").getAsString());
        assertEquals("Full content of the article...", jsonObject.get("fullContent").getAsString());
    }
}
