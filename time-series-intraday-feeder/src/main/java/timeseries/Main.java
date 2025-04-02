package timeseries;

import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import timeseries.controller.IntradayFetcher;

public class Main {
    public static void main(String[] args) {
        if (args.length < 6) {
            System.err.println("Uso: java Main <API_KEY> <DB_URL> <SYMBOL> <INTERVAL> <OUTPUT_SIZE> <FETCH_EVERY_MINUTES>");
            return;
        }

        String apiKey = args[0];
        String dbUrl = args[1];
        String symbol = args[2];
        Interval interval = Interval.valueOf(args[3]);
        OutputSize outputSize = OutputSize.valueOf(args[4]);
        int fetchIntervalMinutes = Integer.parseInt(args[5]);

        IntradayFetcher fetcher = new IntradayFetcher(apiKey, dbUrl, symbol, interval, outputSize);
        fetcher.startFetchingEvery(fetchIntervalMinutes);
    }
}
