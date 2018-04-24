package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
public class Stop<R extends TransportRun<R, ?, P>, P extends Point<P>> {
    /**
     * Порядковый номер остановки транспорта
     */
    private final int number;
    /**
     * Транспорт, который остановился в данной точке
     */
    private final Ref<R> ref;
    /**
     * Точка остановки
     */
    private final Ref<P> point;
    /**
     * Время
     */
    private final Instant time;
}
