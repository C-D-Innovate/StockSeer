package es.ulpgc.dacd.businessunit.domain.model;

import java.time.Instant;

public record NewsEvent(String url, String fullContent, Instant ts) {}
