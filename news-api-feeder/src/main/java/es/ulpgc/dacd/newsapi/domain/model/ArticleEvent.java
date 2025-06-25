package es.ulpgc.dacd.newsapi.domain.model;

import java.time.Instant;

public class ArticleEvent {
    private final String topic;
    private final String ss;
    private final Instant ts;
    private final String url;
    private final Instant publishedAt;
    private final String content;
    private final String title;
    private final String fullContent;

    public ArticleEvent(String topic, String ss, Instant ts, String url, Instant publishedAt,
                        String content, String title, String fullContent) {
        this.topic = topic;
        this.ss = ss;
        this.ts = ts;
        this.url = url;
        this.publishedAt = publishedAt;
        this.content = content;
        this.title = title;
        this.fullContent = fullContent;
    }

    public String getTopic() {
        return topic;
    }

    public Instant getTs() {
        return ts;
    }

    public String getSs() {
        return ss;
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

    public String getFullContent() {
        return fullContent;
    }

}
