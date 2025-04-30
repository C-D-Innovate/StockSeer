package newsapi.domain.model;
import java.time.Instant;
import java.util.Objects;

public class Articles {
    private final String url;
    private final Instant publishedAt;
    private final String content;
    private final String title;

    public Articles(String url, Instant publishedAt, String content, String title) {
        this.url = url;
        this.publishedAt = publishedAt;
        this.content = content;
        this.title = title;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Articles that = (Articles) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "ArticlesData{" +
                "url='" + url + '\'' +
                ", publishedAt=" + publishedAt +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 50)) + "..." : null) + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}