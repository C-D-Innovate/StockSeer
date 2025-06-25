package es.ulpgc.dacd.businessunit.infrastructure.adapters.storage.datamart;

import es.ulpgc.dacd.businessunit.infrastructure.ports.DatamartStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatamartManager implements DatamartStorage {

    private static final Logger logger = LoggerFactory.getLogger(DatamartManager.class);

    private final DatamartConnector datamartConnector;
    private final DatamartWriter datamartWriter;

    public DatamartManager(String dbUrl) {
        this.datamartConnector = new DatamartConnector(dbUrl);
        this.datamartWriter = new DatamartWriter();
        init();
    }

    private void init() {
        try (Connection conn = datamartConnector.getConnection()) {
            DatamartInitializer.createCleanDatamartTable(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error inicializando datamart: " + e.getMessage(), e);
        }
    }

    @Override
    public void mergeToDatamart() {
        try (Connection conn = datamartConnector.getConnection()) {
            datamartWriter.merge(conn);
        } catch (SQLException e) {
            logger.error("Error en mergeToDatamart: {}", e.getMessage(), e);
        }
    }

    @Override
    public void updateAvgSentiment() {
        try (Connection conn = datamartConnector.getConnection()) {
            datamartWriter.updateAvgSentiment(conn);
        } catch (SQLException e) {
            logger.error("Error en updateAvgSentiment: {}", e.getMessage(), e);
        }
    }
}