package com.khovanskiy.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

/**
 * victor
 */
public class Path extends ArrayList<ForwardSegment> {
    public Instant getDepartureTime() {
        return get(0).A().getTime();
    }

    public Instant getArrivalTime() {
        return get(size() - 1).C().getTime();
    }

    public Duration getTime() {
        return Duration.between(getDepartureTime(), getArrivalTime());
    }

    public Path() {
        super();
    }

    public Path(Collection<ForwardSegment> collection) {
        super(collection);
    }

    @Override
    public String toString() {
        String buffer = "Path(size=" + size() + ", depTime=" + getDepartureTime() + ", arrTime=" + getArrivalTime() + ", time=" + getTime().toMinutes() + ")\n";
        for(ForwardSegment segment : this) {
            buffer += "\t" + segment.toString() + "\n";
        }
        return buffer;
    }
}
