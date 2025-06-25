package es.ulpgc.dacd.businessunit.application;

import es.ulpgc.dacd.businessunit.infrastructure.ports.EventStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealTimeEventStarter {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeEventStarter.class);

    private final EventStream eventStream;

    public RealTimeEventStarter(EventStream eventStream) {
        this.eventStream = eventStream;
    }

    public void start() {
        logger.info("Iniciando RealTimeEventStarter...");
        eventStream.start();
    }
}
