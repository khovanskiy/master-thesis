package com.khovanskiy.model;

/**
 * @author victor
 */
public interface SeatAttribute {

    /**
     * Тип вагона
     */
    enum CarriageType implements SeatAttribute {
        /**
         * Плацкартный
         */
        OPEN_PLAN,
        /**
         * Купе
         */
        COUPE,
        /**
         * Сидячий
         */
        LOCAL,
        /**
         * СВ
         */
        HIGH_COMFORT
    }

    interface OpenPlan extends SeatAttribute {
        /**
         * Место
         */
        enum UpperLower implements OpenPlan {
            /**
             * Верхнее
             */
            UPPER,
            /**
             * Нижнее
             */
            LOWER,
        }

        /**
         * Место
         */
        enum Side implements OpenPlan {
            /**
             * Небоковое
             */
            NONSIDE,
            /**
             * Боковое
             */
            SIDE,
        }

        /**
         * Место
         */
        enum WcProximity implements OpenPlan {
            /**
             * Не у туалета
             */
            FAR_FROM_WC,
        }
    }

    interface Coupe extends SeatAttribute {
        /**
         * Место
         */
        enum UpperLower implements Coupe {
            /**
             * Верхнее
             */
            UPPER,
            /**
             * Нижнее
             */
            LOWER,
        }

        /**
         * Место
         */
        enum WcProximity implements Coupe {
            /**
             * Не у туалета
             */
            FAR_FROM_WC,
        }
    }

    interface Local extends SeatAttribute {
        /**
         * Место
         */
        enum Side implements OpenPlan {
            /**
             * У окна
             */
            WINDOW,
            /**
             * У прохода
             */
            PASSAGE,
        }
    }
}
