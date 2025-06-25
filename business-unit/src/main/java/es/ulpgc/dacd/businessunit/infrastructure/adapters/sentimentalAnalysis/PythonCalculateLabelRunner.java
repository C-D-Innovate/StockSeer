package es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonCalculateLabelRunner {
    private static final Logger logger = LoggerFactory.getLogger(PythonCalculateLabelRunner.class);
    private static final int MAX_TEXT_LENGTH = 10000;

    private final PythonExecutor scriptExecutor;

    public PythonCalculateLabelRunner(PythonExecutor scriptExecutor) {
        this.scriptExecutor = scriptExecutor;
    }

    public String runAnalysisScript(String text) {
        if (text == null || text.isBlank()) {
            logger.warn("Texto vacío. Se devuelve NEUTRAL.");
            return "NEUTRAL";
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            logger.warn("Texto demasiado largo ({} caracteres). Truncando.", text.length());
            text = text.substring(0, MAX_TEXT_LENGTH);
        }

        String output = scriptExecutor.executeScriptWithText("/label_news.py", text);
        String label = output.lines().findFirst().orElse("NEUTRAL").trim().toUpperCase();

        if (!label.equals("POSITIVE") && !label.equals("NEGATIVE") && !label.equals("NEUTRAL")) {
            logger.warn("Etiqueta de sentimiento no reconocida: '{}'. Se usará NEUTRAL.", label);
            label = "NEUTRAL";
        }

        logger.info("Etiqueta detectada: {}", label);
        return label;
    }
}