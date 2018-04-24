package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;

/**
 * Номер поезда
 *
 * @author victor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainNumber implements Comparable<TrainNumber>, Serializable {
    private String number;

    @Override
    public int compareTo(TrainNumber o) {
        return ObjectUtils.compare(number, o.number);
    }
}
