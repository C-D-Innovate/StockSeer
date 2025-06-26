package es.ulpgc.dacd.businessunit.infrastructure.ports;

import es.ulpgc.dacd.businessunit.models.MarketEvent;
import es.ulpgc.dacd.businessunit.models.NewsEvent;

public interface EventStorage {
    void saveMarketEvent(MarketEvent event);
    void saveNewsEvent(NewsEvent event);
    boolean containsUrl(String url);
    boolean containsMarket(String symbol, String ts);
}
