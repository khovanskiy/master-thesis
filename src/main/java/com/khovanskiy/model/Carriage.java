package com.khovanskiy.model;

import com.khovanskiy.util.Idx;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author victor
 */
public abstract class Carriage<C extends Carriage<C>> {
    protected Idx<C> idx;
    /**
     * Тип основной
     */
    protected String carriageNumber;
    /**
     * Основной тип
     */
    protected CarriageType mainType;

    public interface Type {

    }

    @Getter
    @EqualsAndHashCode
    public static abstract class Seat<S extends Seat<S>> {
        /**
         * ID места
         */
        protected Idx<S> idx;
        /**
         * ID вагона
         */
        protected Idx<? extends Carriage> carriageIdx;
        /**
         * Номер места
         */
        protected String number;
        /**
         * Номер вагона
         */
        protected String carriageNumber;
    }
}
