package es.ulpgc.dacd.timeseries.domain.model;

import java.time.Instant;

public class AlphaVantageEvent {

    private final String ss;
    private String symbol;
    private Instant ts;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;

    public AlphaVantageEvent(String symbol, Instant timestamp, double open, double high, double low, double close, long volume) {
        this.symbol = symbol;
        this.ts = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.ss = "AlphaVantage";
    }

    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public Instant getTs() {
        return ts;
    }
    public void setTs(Instant timestamp) {
        this.ts = timestamp;
    }
    public String getFormattedTs() {
        return ts.toString();
    }
    public double getOpen() {
        return open;
    }
    public void setOpen(double open) {
        this.open = open;
    }
    public double getHigh() {
        return high;
    }
    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }
    public void setLow(double low) {
        this.low = low;
    }
    public double getClose() {
        return close;
    }
    public void setClose(double close) {
        this.close = close;
    }
    public long getVolume() {
        return volume;
    }
    public void setVolume(long volume) {
        this.volume = volume;
    }
    public String getSs() {
        return ss;
    }
    @Override
    public String toString() {
        return "AlphaVantageEvent{" +
                "symbol='" + symbol + '\'' +
                ", ts=" + ts +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                '}';
    }

}