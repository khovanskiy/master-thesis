package com.khovanskiy.model;

import java.time.ZoneId;

/**
 * @author victor
 */
public class Point<B extends Model<B>> extends Model<B> {
    /**
     * Название
     */
    String name;

    /**
     * Временная зона
     */
    ZoneId zoneId;
}
