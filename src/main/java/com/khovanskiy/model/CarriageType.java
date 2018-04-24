package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Справочник типов вагонов
 *
 * @author victor
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CarriageType extends Model<CarriageType>{
    /**
     * Название
     */
    private String name;
    /**
     * Код
     */
    private String code;

    public CarriageType(Id id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Ref<CarriageType> {
        String id;
    }
}
