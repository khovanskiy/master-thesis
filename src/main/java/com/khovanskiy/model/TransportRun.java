package com.khovanskiy.model;

import com.khovanskiy.util.SegmentTree;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Транспортный рейс
 *
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransportRun<R extends TransportRun<R, W, P>, W extends Waypoint<W, P>, P extends Point<P>> extends BaseService<R> {
    /**
     * Маршрут движения
     */
    protected List<W> waypoints = new ArrayList<>();

    /**
     * Дерево свойств рейса
     */
    protected SegmentTree<Properties> properties = new PropertiesSegmentTree(Properties.empty());

    protected void fillTransportRun(TransportRun run, String name, List<W> waypoints) {
        run.name = name;
        run.waypoints = waypoints;
    }
}
