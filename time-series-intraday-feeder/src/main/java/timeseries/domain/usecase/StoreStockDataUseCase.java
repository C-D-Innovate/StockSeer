package timeseries.domain.usecase;

import timeseries.domain.model.StockData;

import java.util.List;

public class StoreStockDataUseCase {

    private final timeseries.domain.model.StockDataRepository repository;

    public StoreStockDataUseCase(timeseries.domain.model.StockDataRepository repository) {
        this.repository = repository;
    }

    public void store(List<StockData> stockData) {
        for (StockData data : stockData) {
            if (!repository.isDuplicate(data)) {
                repository.saveAll(List.of(data)); // Guardamos de uno en uno para evaluar duplicados
            }
        }
    }
}
