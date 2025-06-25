package es.ulpgc.dacd.timeseries.infrastructure.adapters.provider;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;

public class AlphaVantageInitializer {

    public AlphaVantageInitializer(String apiKey) {
        Config config = Config.builder()
                .key(apiKey)
                .timeOut(10)
                .build();
        AlphaVantage.api().init(config);
    }
}