package utils;

import es.ulpgc.dacd.newsapi.infrastructure.adapters.utils.ArgsParser;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArgsParserTest {

    private static final Path TEMP_FILE = Paths.get("temp_args.txt");

    @BeforeEach
    void setUp() throws IOException {
        Files.write(TEMP_FILE, """
            STORAGE_TARGET=database
            DB_URL=jdbc:sqlite:memory
            QUERY=AAPL
            FROM=2024-06-01
            TO=2024-06-02
        """.getBytes());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TEMP_FILE);
    }

    @Test
    void parse_shouldReturnMapWithCorrectKeysAndValues() {
        Map<String, String> result = ArgsParser.parse(TEMP_FILE.toString());
        assertEquals("database", result.get("STORAGE_TARGET"));
        assertEquals("jdbc:sqlite:memory", result.get("DB_URL"));
        assertEquals("AAPL", result.get("QUERY"));
        assertEquals("2024-06-01", result.get("FROM"));
        assertEquals("2024-06-02", result.get("TO"));
        assertEquals(5, result.size());
    }

    @Test
    void parse_shouldReturnEmptyMapIfFileDoesNotExist() {
        Map<String, String> result = ArgsParser.parse("non_existing_file.txt");


        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parse_shouldIgnoreMalformedLines() throws IOException {

        Files.write(TEMP_FILE, """
            CORRECT_KEY=value
            MALFORMED_LINE
            ANOTHER_KEY=another value
        """.getBytes());

        Map<String, String> result = ArgsParser.parse(TEMP_FILE.toString());

        assertEquals(2, result.size());
        assertEquals("value", result.get("CORRECT_KEY"));
        assertEquals("another value", result.get("ANOTHER_KEY"));
    }
}

