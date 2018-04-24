package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author victor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainCategory extends Model<TrainCategory> {
    /**
     * Название
     */
    private String name;
    /**
     * Номер от
     */
    private int numbersFrom;
    /**
     * Номер до
     */
    private int numbersTo;
}
