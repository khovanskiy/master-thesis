package com.khovanskiy.model.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Абстрактный ответ построителя маршрутов
 *
 * @author victor
 */
@Data
@AllArgsConstructor
public class RouteBuilderResponse<S, F extends RouteBuilderFilter<S>> {

    /**
     * Маршруты с местами и стоимостью
     */
    protected List<S> transportationOfferList;

    /**
     * Номер запроса от RouteBuilder
     */
    protected F filter;

    /**
     * Номер запроса от построителя маршрутов
     */
    protected long requestId;

    public List<S> getRoutes() {
        return transportationOfferList;
    }
}

