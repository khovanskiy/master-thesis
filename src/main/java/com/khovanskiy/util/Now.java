package com.khovanskiy.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Date;

/**
 * @author victor
 */
public class Now {
    private static Clock clock = Clock.systemDefaultZone();

    public static LocalDate localDate() {
        return LocalDate.now(clock);
    }

    public static LocalDateTime localDateTime() {
        return LocalDateTime.now(clock);
    }

    public static Instant instant() {
        return Instant.now(clock);
    }

    public static Date date() {
        return Date.from(instant());
    }

    public static YearMonth yearMonth() {
        return YearMonth.now(clock);
    }

    public static Clock getClock() {
        return clock;
    }

    public static void setClock(Clock clock) {
        Now.clock = clock;
    }
}
