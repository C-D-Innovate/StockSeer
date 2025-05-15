package es.ulpgc.dacd.businessunit.domain.model;

import java.time.Instant;

public record MarketEvent(String symbol, double price, long volume, Instant ts) {}
