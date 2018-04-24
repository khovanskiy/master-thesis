package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author victor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Train extends Model<Train> {

    /**
     * Тип тяги
     */
    private TypeOfEngine typeOfEngine;
    /**
     * Номер поезда
     */
    private TrainNumber number;
    /**
     * Категория поезда
     */
    private TrainCategory trainCategory;
    /**
     * Бренд поезда
     */
    private TrainBrand trainBrand;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Ref<Train> {
        String number;
    }
}
