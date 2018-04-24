package com.khovanskiy.model;

import java.util.Collection;
import java.util.List;

/**
 * victor
 */
@FunctionalInterface
public interface Transfers {
    List<ForwardSegment> successors(Collection<Ref<? extends Point>> departures, Stop currentStop, Collection<Ref<? extends Point>> arrivals);
}
