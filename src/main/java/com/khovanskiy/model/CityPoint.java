package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.ZoneId;
import java.util.List;

/**
 * @author victor
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CityPoint extends Point<CityPoint> {
    private static final long serialVersionUID = 467172015693182866L;
    private List<Ref<? extends Point>> refPoints;

    public CityPoint(String id, String name, ZoneId zoneId, List<Ref<? extends Point>> refPoints) {
        this.id = new Id(id);
        this.name = name;
        this.zoneId = zoneId;
        this.refPoints = refPoints;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Ref<CityPoint> {
        String id;
    }
}
