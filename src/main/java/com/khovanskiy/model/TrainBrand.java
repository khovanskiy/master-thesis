package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;

/**
 * Бренд поезда
 *
 * @author victor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainBrand implements Comparable<TrainBrand>, Serializable {
    protected String name;

    @Override
    public int compareTo(TrainBrand o) {
        return ObjectUtils.compare(name, o.name);
    }
}
