package es.ulpgc.dacd.businessunit.infrastructure.ports;

public interface DatamartStoragePort {
    void mergeToDatamart();
    void updateAvgSentiment();
}

