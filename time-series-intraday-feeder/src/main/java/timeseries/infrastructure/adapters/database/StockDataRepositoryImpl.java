package timeseries.infrastructure.adapters.database;

import timeseries.domain.model.StockData;
import timeseries.domain.model.StockDataRepository;
import timeseries.infrastructure.DatabaseManager;

import java.util.List;

public class StockDataRepositoryImpl implements StockDataRepository {

    private final String dbUrl;

    public StockDataRepositoryImpl(String dbUrl) {
        this.dbUrl = dbUrl;
        DatabaseManager.initializeDatabase(dbUrl);
    }

    @Override
    public void saveAll(List<StockData> data) {
        for (StockData d : data) {
            DatabaseManager.insert(d, dbUrl);
        }
    }

    @Override
    public boolean isDuplicate(StockData data) {
        return DatabaseManager.isDuplicateData(data, dbUrl);
    }
}
