package es.ulpgc.dacd.businessunit.models;

import java.time.Instant;
public record NewsEvent(String url, String content, Instant ts, String fullContent, String sentimentLabel) {}

