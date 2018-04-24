package com.khovanskiy.model;

import com.khovanskiy.util.Idx;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Маршрутная точка
 *
 * @author victor
 */
@Getter
public class Waypoint<W extends Waypoint<W, P>, P extends Point<P>> {
    /**
     * ID маршрутной точки
     */
    protected Idx<W> idx;
    /**
     * ID станции
     */
    protected Ref<P> point;
    /**
     * Время прибытия
     */
    protected Instant arrival;
    /**
     * Время отправления
     */
    protected Instant departure;

    public boolean isNullStop() {
        return Objects.equals(arrival, departure);
    }

    public void fillWaypoint(Waypoint<W, P> w, Idx<W> idx, Ref<P> point, Instant arrival, Instant departure) {
        w.idx = idx;
        w.point = point;
        w.arrival = arrival;
        w.departure = departure;
    }
}
