package es.ulpgc.dacd.businessunit.models;

import java.time.Instant;

public record MarketEvent(String symbol, double price, long volume, Instant ts) {}
