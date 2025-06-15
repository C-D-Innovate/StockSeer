package es.ulpgc.dacd.businessunit.application;

import es.ulpgc.dacd.businessunit.infrastructure.ports.EventStream;

public class RealTimeSyncService {

    private final EventStream eventStream;

    public RealTimeSyncService(EventStream eventStream) {
        this.eventStream = eventStream;
    }

    public void start() {
        System.out.println("Iniciando RealTimeSyncService...");
        eventStream.start();
    }
}
