package es.ulpgc.dacd.eventstorebuilder.model;

import java.time.Instant;

public class Event {

    private final Instant ts;
    private final String ss;
    private final String topic;
    private final String json;

    public Event(Instant ts, String ss, String topic, String json) {
        this.ts = ts;
        this.ss = ss;
        this.topic = topic;
        this.json = json;
    }

    public Instant getTs() {
        return ts;
    }

    public String getSs() {
        return ss;
    }

    public String getTopic() {
        return topic;
    }

    public String getJson() {
        return json;
    }
}
