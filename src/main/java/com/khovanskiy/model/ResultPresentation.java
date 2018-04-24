package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultPresentation {
    /**
     * Порядок сортировки
     */
    @NonNull
    SortOrder sortOrder;

    /**
     * Направление сортировки
     */
    @NonNull
    SortDirection sortDirection;

    /**
     * Количество результатов
     */
    @NonNull
    int numberOfResult;

    public enum SortOrder {
        /**
         * По количеству пересадок
         */
        TRANSFERS,
        /**
         * По времени в пути
         */
        TIME,
        /**
         * По дате отправления
         */
        DEPARTURE,
        /**
         * По дате прибытия
         */
        ARRIVAL,
        /**
         * По цене
         */
        PRICE
    }

    public enum SortDirection {
        ASC, DESC
    }

    public static ResultPresentation createDefault() {
        return new ResultPresentation(SortOrder.DEPARTURE, SortDirection.ASC, 10);
    }
}
