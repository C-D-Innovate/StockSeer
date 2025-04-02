package timeseries.infrastructure;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;

public class AlphaVantageInitializer {

    public static void init(String apiKey) {
        Config cfg = Config.builder()
                .key(apiKey)
                .timeOut(10)
                .build();
        AlphaVantage.api().init(cfg);
    }
}
