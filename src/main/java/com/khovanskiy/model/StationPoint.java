package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.ZoneId;

/**
 * @author victor
 */
public class StationPoint extends Point<StationPoint> {
    public StationPoint(String id, String name, ZoneId zoneId) {
        this.id = new Id(id);
        this.name = name;
        this.zoneId = zoneId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Ref<StationPoint> {
        String id;
    }
}
