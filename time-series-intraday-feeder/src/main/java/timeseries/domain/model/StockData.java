package timeseries.domain.model;

public class StockData {
    private final String symbol;
    private final String timestamp;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;

    public StockData(String symbol, String timestamp, double open, double high, double low, double close, long volume) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public String getSymbol() { return symbol; }
    public String getTimestamp() { return timestamp; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public long getVolume() { return volume; }
}

