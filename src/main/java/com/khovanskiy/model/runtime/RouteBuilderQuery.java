package com.khovanskiy.model.runtime;

import com.khovanskiy.model.PointTimeInterval;
import com.khovanskiy.model.ResultPresentation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.function.Predicate;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
public class RouteBuilderQuery<Q extends RouteBuilderQuery<Q, F, S>, F extends Predicate<S>, S> {
    /**
     * Отправление
     */
    protected PointTimeInterval departure;

    @NonNull
    /**
     * Прибытие
     */
    protected PointTimeInterval arrival;

    @NonNull
    /**
     * Требования к маршрутам
     */
    protected F filter;

    @NonNull
    /**
     * Порядок сортировки результатов поиска
     */
    protected ResultPresentation resultPresentation;

    /**
     * Номер запроса
     */
    public long requestId;
}
