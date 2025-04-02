package timeseries.domain.usecase;

import timeseries.domain.model.StockData;

import java.util.List;

public interface FetchStockDataUseCase {
    List<StockData> fetch(String symbol);
}
