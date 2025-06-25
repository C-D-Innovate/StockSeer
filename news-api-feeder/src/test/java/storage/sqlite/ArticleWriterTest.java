package storage.sqlite;

import es.ulpgc.dacd.newsapi.domain.model.ArticleEvent;
import es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.SQLite.ArticleWriter;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArticleWriterTest {

    private Connection connection;
    private ArticleWriter writer;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        writer = new ArticleWriter(connection);
        createTable();
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE articles (
                url TEXT,
                publishedAt TEXT,
                title TEXT,
                fullContent TEXT
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Test
    void insertArticles_shouldInsertAllArticles() throws SQLException {
        ArticleEvent article1 = new ArticleEvent(
                "tech", "ss1", Instant.now(), "http://example.com/1",
                Instant.parse("2025-06-25T08:00:00Z"), "short content 1",
                "Title 1", "Full content 1"
        );

        ArticleEvent article2 = new ArticleEvent(
                "finance", "ss2", Instant.now(), "http://example.com/2",
                Instant.parse("2025-06-25T09:00:00Z"), "short content 2",
                "Title 2", "Full content 2"
        );

        int inserted = writer.insertArticles(List.of(article1, article2));
        assertEquals(2, inserted);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM articles")) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
        }
    }

    @Test
    void insertArticles_shouldUseFallbackWhenFullContentIsBlank() throws SQLException {
        ArticleEvent article = new ArticleEvent(
                "tech", "ss3", Instant.now(), "http://example.com/3",
                Instant.parse("2025-06-25T10:00:00Z"), "fallback content",
                "Title 3", ""
        );

        int inserted = writer.insertArticles(List.of(article));
        assertEquals(1, inserted);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT fullContent FROM articles WHERE url = 'http://example.com/3'")) {
            assertTrue(rs.next());
            assertEquals("fallback content", rs.getString("fullContent"));
        }
    }
}
