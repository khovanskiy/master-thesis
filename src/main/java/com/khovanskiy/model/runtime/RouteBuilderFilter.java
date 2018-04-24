package com.khovanskiy.model.runtime;

import java.util.function.Predicate;

/**
 * Абстрактный фильтр маршрутов
 *
 * @author victor
 */
public interface RouteBuilderFilter<S> extends Predicate<S> {
    int getMaxTransfersCount();

    int getRequiredQuantity();
}
