package com.khovanskiy.model;

/**
 * @author victor
 */
public class TrainCarriage<C extends TrainCarriage<C>> extends PassengerCarriage<C> {
    public enum Type implements Carriage.Type {
        /**
         * Плацкартный
         */
        OPEN_PLAN,
        /**
         * Купе
         */
        COUPE,
        /**
         * СВ
         */
        HIGH_COMFORT,
        /**
         * Высокоскоростной
         */
        HIGH_SPEED,
        /**
         * Сидячий
         */
        LOCAL
    }
}
