package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author victor
 */
@Data
@NoArgsConstructor
public class RailwayCarrier extends Model<RailwayCarrier>{
    String fullName;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Ref<RailwayCarrier> {
        String id;
    }
}
