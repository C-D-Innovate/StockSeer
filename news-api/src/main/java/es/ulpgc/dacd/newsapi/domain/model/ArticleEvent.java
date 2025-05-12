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
        this(url, publishedAt, content, title, null, null, Instant.now());
    }

    private ArticleEvent(String url, Instant publishedAt, String content, String title, String topic, String ss, Instant ts) {
        this.url = url;
        this.publishedAt = publishedAt;
        this.content = content;
        this.title = title;
        this.topic = topic;
        this.ss = ss;
        this.ts = ts;
    }

    public String getTopic() { return topic; }
    public String getSs() { return ss; }
    public Instant getTs() { return ts; }
    public String getUrl() { return url; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getContent() { return content; }
    public String getTitle() { return title; }

    @Override
    public String toString() {
        return "ArticleEvent{" +
                "topic='" + topic + '\'' +
                ", ss='" + ss + '\'' +
                ", ts=" + ts +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
