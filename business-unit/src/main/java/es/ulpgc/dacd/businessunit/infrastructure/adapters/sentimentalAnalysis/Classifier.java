package es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Classifier {
    private static final Logger logger = LoggerFactory.getLogger(Classifier.class);
    private final PythonCalculateLabelRunner pythonRunner;

    public Classifier(PythonCalculateLabelRunner pythonRunner) {
        this.pythonRunner = pythonRunner;
    }

    public String labelFrom(String text) {
        try {
            return pythonRunner.runAnalysisScript(text);
        } catch (Exception e) {
            logger.error("Error ejecutando análisis de sentimiento: {}", e.getMessage(), e);
            return "NEUTRAL";
        }
    }
}