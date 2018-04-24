package com.khovanskiy.model;

import lombok.Data;

/**
 * Облегченная остановка
 *
 * victor
 */
@Data
public class StopLight<R extends TransportRun<R, ?, P>, P extends Point<P>> {
    /**
     * Порядковый номер остановки транспорта
     */
    private final int number;
    /**
     * Транспорт, который остановился в данной точке
     */
    private final Ref<R> ref;
}
