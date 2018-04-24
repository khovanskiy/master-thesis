package com.khovanskiy.model;

import com.khovanskiy.util.InstantInterval;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
public class PointTimeInterval {
    /**
     * ID транспортного узла
     */
    protected Ref<? extends Point> point;

    /**
     * Временной интервал
     */
    protected InstantInterval timeInterval;
}
