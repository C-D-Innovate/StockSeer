package es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalculateLabel {
    private static final Logger logger = LoggerFactory.getLogger(CalculateLabel.class);
    private final PythonCalculateLabelRunner pythonRunner;

    public CalculateLabel(PythonCalculateLabelRunner pythonRunner) {
        this.pythonRunner = pythonRunner;
    }

    public String labelFrom(String text) {
        try {
            return pythonRunner.runAnalysisScript(text);
        } catch (Exception e) {
            logger.error("Error ejecutando análisis de sentimiento: {}", e.getMessage(), e);
            return "unknown";
        }
    }
}
