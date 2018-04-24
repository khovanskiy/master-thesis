package com.khovanskiy.model;

import com.khovanskiy.util.Idx;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Плацкартный вагон
 *
 * @author victor
 */
public class OpenPlanCarriage extends TrainCarriage<OpenPlanCarriage> {

    /**
     * Купе
     */
    protected List<Coupe> coupes;

    public OpenPlanCarriage(
            Idx<OpenPlanCarriage> idx,
            String carriageNum,
            CarriageType mainType,
            List<Coupe> coupes) {
        this.idx = idx;
        this.carriageNumber = carriageNum;
        this.mainType = mainType;
        this.coupes = coupes;
    }

    @Getter
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
        public Seat(Idx<Seat> seatIdx, String seatNum,
                    Idx<? extends Carriage> carriageIdx, String carriageNum) {
            this.idx = seatIdx;
            this.number = seatNum;
            this.carriageIdx = carriageIdx;
            this.carriageNumber = carriageNum;
        }
    }
}
