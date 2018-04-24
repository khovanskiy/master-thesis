package com.khovanskiy.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Железнодорожный рейс
 *
 * @author victor
 */
public abstract class RailwayRun<B extends TransportRun<B, RailwayWaypoint, StationPoint>>
        extends TransportRun<B, RailwayWaypoint, StationPoint> {
    /**
     * Перевозчик
     */
    protected Ref<RailwayCarrier> carrierId;
    /**
     * Глубина продажи
     */
    protected int saleDepth;
    /**
     * Вагоны
     */
    protected List<Carriage> carriages;

    public void fillRailwayRun(RailwayRun run,
                               Ref<RailwayCarrier> carrierId,
                               int saleDepth,
                               List<? extends Carriage> carriages) {
        run.carrierId = carrierId;
        run.saleDepth = saleDepth;
        run.carriages = carriages;
    }

    /**
     * Состав следования
     */
    @Getter
    @NoArgsConstructor
    public static class CarryingTrain {
        /**
         * Поезд
         */
        protected Train train;
        /**
         * Признак фирменности
         */
        protected boolean firmed;

        public CarryingTrain(Train train, boolean firmed) {
            this.train = train;
            this.firmed = firmed;
        }
    }
}
