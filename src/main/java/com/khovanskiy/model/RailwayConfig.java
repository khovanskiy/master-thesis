package com.khovanskiy.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author victor
 */
@Getter
@Setter
public abstract class RailwayConfig<B extends TransportConfig<B>> extends TransportConfig<B> {
    /**
     * Перевозчик
     */
    Ref<RailwayCarrier> carrier;
}
