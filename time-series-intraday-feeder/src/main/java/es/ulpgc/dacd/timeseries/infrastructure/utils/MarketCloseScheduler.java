package es.ulpgc.dacd.timeseries.infrastructure.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class MarketCloseScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MarketCloseScheduler.class);

    private final ZoneId zoneId;
    private final LocalTime marketClose;
    private final Timer timer;

    public MarketCloseScheduler(ZoneId zoneId, LocalTime marketClose) {
        this.zoneId = zoneId;
        this.marketClose = marketClose;
        this.timer = new Timer();
    }

    public void start(Runnable task) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now(zoneId).withSecond(0).truncatedTo(ChronoUnit.MINUTES);
                logger.info("[Scheduler] Hora actual: {}. Esperando hora de cierre: {}", now, marketClose);

                if (now.equals(marketClose)) {
                    logger.info("[Scheduler] Hora de cierre alcanzada. Ejecutando tarea.");
                    try {
                        task.run();
                    } catch (Exception e) {
                        logger.error("[Scheduler] Error durante la ejecución de la tarea:", e);
                    } finally {
                        timer.cancel();
                        logger.info("[Scheduler] Timer detenido tras la ejecución.");
                    }
                }
            }
        }, 0, 60_000);
    }
}