package com.khovanskiy.model;

import com.khovanskiy.util.Idx;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Вагон повышенной комфортности (СВ)
 *
 * @author victor
 */
public class HighComfortCarriage extends TrainCarriage<HighComfortCarriage> {

    /**
     * Купе повышенной комфортности
     */
    protected List<Coupe> coupes;

    public HighComfortCarriage(
            Idx<HighComfortCarriage> idx,
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
         * Места повышенной комфортности
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
