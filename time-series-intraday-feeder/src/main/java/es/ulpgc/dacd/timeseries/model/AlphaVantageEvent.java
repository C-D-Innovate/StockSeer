package es.ulpgc.dacd.timeseries.model;

import java.time.Instant;

public class AlphaVantageEvent {

    private final String topic = "StockQuotes";
    private final String ss = "AlphaVantageFeeder";
    private final Instant ts;
    private final String symbol;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;

    public AlphaVantageEvent(String symbol, Instant ts, double open, double high, double low, double close, long volume) {
        this.ts = ts;
        this.symbol = symbol;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public String getSymbol() {
        return symbol;
    }

    public Instant getTs() {
        return ts;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }


    @Override
    public String toString() {
        return String.format("AlphaVantageEvent{topic='%s', ss='%s', ts=%s, symbol='%s', open=%.2f, high=%.2f, low=%.2f, close=%.2f, volume=%d}",
                topic, ss, ts, symbol, open, high, low, close, volume);
    }
}