package timeseries.infrastructure.adapters.api;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import timeseries.domain.model.StockData;
import timeseries.domain.usecase.FetchStockDataUseCase;
import timeseries.infrastructure.AlphaVantageInitializer;
import java.util.ArrayList;
import java.util.List;

public class AlphaVantageAPIAdapter implements FetchStockDataUseCase {

    private final String apiKey;
    private final Interval interval;
    private final OutputSize outputSize;

    public AlphaVantageAPIAdapter(String apiKey, Interval interval, OutputSize outputSize) {
        this.apiKey = apiKey;
        this.interval = interval;
        this.outputSize = outputSize;
    }

    @Override
    public List<StockData> fetch(String symbol) {
        AlphaVantageInitializer.init(apiKey);

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
    }
}

