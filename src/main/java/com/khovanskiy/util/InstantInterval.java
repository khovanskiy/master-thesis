package com.khovanskiy.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.time.Instant;

/**
 * Интервал между двумя точками во времени
 *
 * @author victor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class InstantInterval {
    /**
     * Дата и время начала
     */
    protected Instant since;

    /**
     * Дата и время конца
     */
    protected Instant till;

    public static InstantInterval full() {
        return new InstantInterval(null, null);
    }

    public static InstantInterval since(Instant instant) {
        return new InstantInterval(instant, null);
    }

    public static InstantInterval till(Instant instant) {
        return new InstantInterval(null, instant);
    }
}
