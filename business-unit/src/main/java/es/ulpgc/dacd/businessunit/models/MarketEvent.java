package es.ulpgc.dacd.businessunit.models;

import java.time.Instant;

public record MarketEvent(String symbol, long volume, Instant open_ts, double open, Instant close_ts, double close) {}
