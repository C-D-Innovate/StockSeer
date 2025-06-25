package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datamart;

import es.ulpgc.dacd.businessunit.infrastructure.ports.DatamartStoragePort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatamartStorageManager implements DatamartStoragePort {

    private static final Logger logger = LoggerFactory.getLogger(DatamartStorageManager.class);

    private final DatamartConnection datamartConnection;
    private final DatamartWriter datamartWriter;

    public DatamartStorageManager(String dbUrl) {
        this.datamartConnection = new DatamartConnection(dbUrl);
        this.datamartWriter = new DatamartWriter();
        init();
    }

    private void init() {
        try (Connection conn = datamartConnection.getConnection()) {
            DatamartInitializer.createCleanDatamartTable(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error inicializando datamart: " + e.getMessage(), e);
        }
    }

    @Override
    public void mergeToDatamart() {
        try (Connection conn = datamartConnection.getConnection()) {
            datamartWriter.merge(conn);
        } catch (SQLException e) {
            logger.error("Error en mergeToDatamart: {}", e.getMessage(), e);
        }
    }

    @Override
    public void updateAvgSentiment() {
        try (Connection conn = datamartConnection.getConnection()) {
            datamartWriter.updateAvgSentiment(conn);
        } catch (SQLException e) {
            logger.error("Error en updateAvgSentiment: {}", e.getMessage(), e);
        }
    }
}