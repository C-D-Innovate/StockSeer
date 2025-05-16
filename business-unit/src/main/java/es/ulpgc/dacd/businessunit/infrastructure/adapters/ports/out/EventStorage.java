package es.ulpgc.dacd.businessunit.infrastructure.adapters.ports.out;

import es.ulpgc.dacd.businessunit.domain.model.MarketEvent;
import es.ulpgc.dacd.businessunit.domain.model.NewsEvent;

public interface EventStorage {
    void saveMarketEvent(MarketEvent event);
    void saveNewsEvent(NewsEvent event);
}
