package com.khovanskiy.model;

import com.khovanskiy.util.Idx;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Вагон купе
 *
 * @author victor
 */
public class CoupeCarriage extends TrainCarriage<CoupeCarriage> {

    /**
     * Купе
     */
    protected List<Coupe> coupes;

    public CoupeCarriage(
            Idx<CoupeCarriage> idx,
            String carriageNum,
            CarriageType mainType,
            List<Coupe> coupes) {

        this.idx = idx;
        this.carriageNumber = carriageNum;
        this.mainType = mainType;
        this.coupes = coupes;
    }

    /**
     * Купе
     */
    @Getter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Coupe {
        /**
         * Номер
         */
        protected int number;
        /**
         * Места
         */
        protected List<Seat> seats;

        public Coupe(int number, List<Seat> seats) {
            this.number = number;
            this.seats = seats;
        }
    }

    @Getter
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Seat extends Carriage.Seat<Seat> {

        public Seat(Idx seatIdx, String seatNum,
                    Idx<? extends Carriage> carriageIdx, String carriageNum) {
            this.idx = seatIdx;
            this.number = seatNum;
            this.carriageIdx = carriageIdx;
            this.carriageNumber = carriageNum;
        }
    }
}
