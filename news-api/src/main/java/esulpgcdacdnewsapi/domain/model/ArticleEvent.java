package esulpgcdacdnewsapi.domain.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class ArticleEvent {
    private final String topic;
    private final String ss;
    private final Instant ts;
    private final String url;
    private final Instant publishedAt;
    private final String content;
    private final String title;

    public ArticleEvent(String topic, String ss, Instant ts, String url, Instant publishedAt, String content, String title) {
        this.topic = topic;
        this.ss = ss;
        this.ts = ts;
        this.url = url;
        this.publishedAt = publishedAt;
        this.content = content;
        this.title = title;
    }

    public ArticleEvent(String url, Instant publishedAt, String content, String title) {
        this.url = url;
        this.publishedAt = publishedAt;
        this.content = content;
        this.title = title;
        this.topic = null;
        this.ss = null;
        this.ts = Instant.now(); // timestamp actual como fallback
    }


    public String getTopic() {
        return topic;
    }

    public String getSs() {
        return ss;
    }

    public Instant getTs() {
        return ts;
    }

    public String getUrl() {
        return url;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public String getStoragePath() {
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").format(ts);
        return String.format("eventstore/%s/%s/%s.events", topic, ss, date);
    }

    @Override
    public String toString() {
        return "ArticleEvent{" +
                "topic='" + topic + '\'' +
                ", ss='" + ss + '\'' +
                ", ts=" + ts +
                ", url='" + url + '\'' +
                ", publishedAt=" + publishedAt +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 50)) + "..." : null) + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>)
                        (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .create();

        return gson.toJson(this);
    }
}
