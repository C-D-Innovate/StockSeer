package timeseries.infrastructure.adapters.api;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import timeseries.domain.model.StockData;

import java.util.ArrayList;
import java.util.List;

public class AlphaVantageAPI {

    private final Interval interval;
    private final OutputSize outputSize;

    public AlphaVantageAPI(String apiKey, Interval interval, OutputSize outputSize) {
        Config cfg = Config.builder()
                .key(apiKey)
                .timeOut(10)
                .build();
        AlphaVantage.api().init(cfg);
        this.interval = interval;
        this.outputSize = outputSize;
    }


    public List<StockData> fetch(String symbol) {
        try {
            TimeSeriesResponse response = AlphaVantage.api()
                    .timeSeries()
                    .intraday()
                    .forSymbol(symbol)
                    .interval(interval)
                    .outputSize(outputSize)
                    .fetchSync();

            List<StockData> stockDataList = new ArrayList<>();
            if (response != null && response.getStockUnits() != null) {
                for (StockUnit unit : response.getStockUnits()) {
                    StockData data = new StockData(
                            symbol,
                            unit.getDate(),
                            unit.getOpen(),
                            unit.getHigh(),
                            unit.getLow(),
                            unit.getClose(),
                            unit.getVolume()
                    );
                    stockDataList.add(data);
                }
            }

            return stockDataList;

        } catch (Exception e) {
            System.err.println("Error al obtener los datos de la API: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
