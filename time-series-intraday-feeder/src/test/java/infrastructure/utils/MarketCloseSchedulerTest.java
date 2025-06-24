package infrastructure.utils;

import es.ulpgc.dacd.timeseries.infrastructure.utils.MarketCloseScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.TimerTask;

import static org.junit.jupiter.api.Assertions.*;

class MarketCloseSchedulerTest {

    private final ZoneId zone = ZoneId.of("UTC");
    private MarketCloseScheduler scheduler;
    private FakeTimer fakeTimer;

    @BeforeEach
    void setUp() throws Exception {
        LocalTime nowTrunc = LocalTime.now(zone)
                .withSecond(0)
                .truncatedTo(ChronoUnit.MINUTES);
        scheduler = new MarketCloseScheduler(zone, nowTrunc);

        fakeTimer = new FakeTimer();
        Field timerField = MarketCloseScheduler.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        timerField.set(scheduler, fakeTimer);
    }

    @Test
    void start_ProgramaFixedRateYCapturaTimerTask() {
        scheduler.start(() -> {});

        assertNotNull(fakeTimer.scheduledTask, "Debe haber capturado el TimerTask");
        assertEquals(0L, fakeTimer.delay,     "El delay inicial debe ser 0");
        assertEquals(60_000L, fakeTimer.period, "El periodo debe ser de 60 000 ms");
    }

    @Test
    void runTask_CuandoHoraNoCoincide_NoEjecutaTareaNiCancelaTimer() throws Exception {
        LocalTime later = LocalTime.now(zone)
                .plusMinutes(1)
                .withSecond(0)
                .truncatedTo(ChronoUnit.MINUTES);
        scheduler = new MarketCloseScheduler(zone, later);
        Field timerField = MarketCloseScheduler.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        timerField.set(scheduler, fakeTimer);

        boolean[] ejecutado = {false};
        scheduler.start(() -> ejecutado[0] = true);

        TimerTask task = fakeTimer.scheduledTask;
        task.run();

        assertFalse(ejecutado[0],              "No debe ejecutar la tarea si la hora no coincide");
        assertFalse(fakeTimer.cancelled,       "No debe cancelar el Timer si la hora no coincide");
    }

    @Test
    void runTask_CuandoHoraCoincide_EjecutaTareaYCancelaTimer() {
        boolean[] ejecutado = {false};
        scheduler.start(() -> ejecutado[0] = true);

        TimerTask task = fakeTimer.scheduledTask;
        task.run();

        assertTrue(ejecutado[0],               "Debe ejecutar la tarea cuando la hora coincide");
        assertTrue(fakeTimer.cancelled,        "Debe cancelar el Timer tras ejecutar la tarea");
    }

    static class FakeTimer extends java.util.Timer {
        TimerTask scheduledTask;
        long delay, period;
        boolean cancelled = false;

        @Override
        public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
            this.scheduledTask = task;
            this.delay = delay;
            this.period = period;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }
    }
}