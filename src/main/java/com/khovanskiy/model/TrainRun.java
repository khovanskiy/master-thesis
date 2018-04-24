package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Рейс дальнего следования
 *
 * @author victor
 */
public class TrainRun extends RailwayRun<TrainRun> {
    public TrainRun(Ref<TrainRun> id, String name, List<RailwayWaypoint> waypoints) {
        this.id = id;
        this.state = State.ACTIVE;
        fillTransportRun(this, name, waypoints);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static final class Id implements Ref<TrainRun> {
        String id;
    }
}
