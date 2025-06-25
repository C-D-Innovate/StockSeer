package es.ulpgc.dacd.businessunit.infrastructure.ports;

public interface DatamartStorage {
    void mergeToDatamart();
    void updateAvgSentiment();
}