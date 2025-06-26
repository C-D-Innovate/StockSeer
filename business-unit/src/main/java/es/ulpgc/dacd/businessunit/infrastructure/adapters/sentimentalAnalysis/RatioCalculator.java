package es.ulpgc.dacd.businessunit.infrastructure.adapters.sentimentalAnalysis;

public class RatioCalculator {

    public double calculateRatio(String[] sentimentLabels) {
        if (sentimentLabels == null || sentimentLabels.length == 0) return 0.0;

        double total = 0.0;
        int count = 0;

        for (String label : sentimentLabels) {
            switch (label.trim().toUpperCase()) {
                case "POSITIVE": total += 1.0; count++; break;
                case "NEUTRAL":  total += 0.0; count++; break;
                case "NEGATIVE": total -= 1.0; count++; break;
                default: break;
            }
        }
        return count > 0 ? total / count : 0.0;
    }
}