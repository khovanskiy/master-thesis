package com.khovanskiy.model;

import com.khovanskiy.util.Idx;

import java.time.Instant;

/**
 * Железнодорожная маршрутная точка
 *
 * @author victor
 */
public class RailwayWaypoint extends Waypoint<RailwayWaypoint, StationPoint> {
    /**
     * Поезд отправления
     */
    protected RailwayRun.CarryingTrain departureTrain;
    /**
     * Расстояние, которое прошла группа вагонов от начала движения ГВ, км
     */
    protected double distance;

    public RailwayWaypoint(Idx<RailwayWaypoint> idx, Ref<StationPoint> stationPointId, Instant arrival, Instant departure,
                           RailwayRun.CarryingTrain departureTrain,
                           double distance) {

        this.departureTrain = departureTrain;
        this.distance = distance;

        fillWaypoint(this, idx, stationPointId, arrival, departure);
    }
}
