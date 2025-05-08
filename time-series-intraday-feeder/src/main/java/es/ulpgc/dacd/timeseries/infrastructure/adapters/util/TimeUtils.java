package es.ulpgc.dacd.timeseries.infrastructure.adapters.util;

import java.time.LocalTime;

public class TimeUtils {
    public static boolean isWithinOneMinuteOf(LocalTime a, LocalTime b) {
        return Math.abs(a.toSecondOfDay() - b.toSecondOfDay()) <= 60;
    }
}