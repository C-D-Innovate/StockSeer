package timeseries.domain.model;

import java.util.List;

public interface StockDataRepository {
    void saveAll(List<StockData> data);
    boolean isDuplicate(StockData data);
}

