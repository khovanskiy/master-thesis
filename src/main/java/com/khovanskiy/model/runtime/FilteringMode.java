package com.khovanskiy.model.runtime;

/**
 * Методы фильтрации
 *
 * @author victor
 */
public enum FilteringMode {
    /**
     * Метод отфильтровывает те значения, которые есть в списке значений, но не выбраны
     */
    BlackList,
    /**
     * Метод отфильтровывает как не выбранные, так и не представленные в списке значения
     */
    WhiteList
}
