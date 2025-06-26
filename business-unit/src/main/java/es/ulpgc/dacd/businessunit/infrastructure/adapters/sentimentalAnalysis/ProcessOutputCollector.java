package es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis;

import java.io.*;
import java.util.logging.Logger;

class ProcessOutputCollector extends Thread {
    private static final Logger logger = Logger.getLogger(ProcessOutputCollector.class.getName());
    private final InputStream inputStream;
    private final StringBuilder content = new StringBuilder();

    public ProcessOutputCollector(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            logger.warning("Error leyendo el stream: " + e.getMessage());
        }
    }

    public String getContent() {
        return content.toString();
    }
}
