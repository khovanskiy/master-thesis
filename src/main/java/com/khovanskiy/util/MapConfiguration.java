package com.khovanskiy.util;

import com.khovanskiy.model.SeatAttribute;
import com.khovanskiy.model.TrainBrand;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

/**
 * @author victor
 */
@Data
public class MapConfiguration {
    /**
     * Пространственный разброс по X
     */
    private Pair<Integer, Integer> spreadX = Pair.of(0, 50);
    /**
     * Пространственный разброс по Y
     */
    private Pair<Integer, Integer> spreadY = Pair.of(0, 50);
    /**
     * Максимальное расстояние между соседними станциями
     */
    private int maxNeighboursRadius = 10;
    /**
     * Максимальное количество соседних станций
     */
    private int maxNeighboursCount = 15;
    /**
     * Количество точек (станции, аэропорты и т.д.)
     */
    private int pointsCount = 5;
    /**
     * Количество хабов
     */
    private int hubsCount = 0;
    /**
     * Максимальное расстояние от центра хаба
     */
    private int maxHubsRadius = 20;
    /**
     * Разброс количества станций, которые могут быть в хабе
     */
    private Pair<Integer, Integer> hubSizeSpread = Pair.of(2, 5);
    /**
     * Количество поездов
     */
    private int maxTrainsCount = 15;
    /**
     * Количество уникальных маршрутов
     */
    private int maxRunsCount = 15;
    /**
     * Разброс количества точек, через которые проходит маршрут
     */
    private Pair<Integer, Integer> pointsInRunSpread = Pair.of(5, 15);
    /**
     * Интервал времени, в пределах которого будут дублироваться уникальные маршруты со смещением по времени
     */
    private Pair<Integer, Integer> trainWaitingSpread = Pair.of(0, 5);
    private InstantInterval timeInterval = new InstantInterval();
    /**
     * Бренды поездов.
     */
    private List<TrainBrand> trainBrands;
    /**
     * Названия категорий поездов.
     */
    private List<String> trainCategoryNames;
    /**
     * Названия железнодорожных перевозчиков.
     */
    private List<String> railwayCarrierNames;
    /**
     * Разброс количества вагонов.
     */
    private Pair<Integer, Integer> carriagesSpread;
    /**
     * Разброс количества купе.
     */
    private Pair<Integer, Integer> coupesSpread;
    /**
     * Разброс количества мест.
     */
    private Pair<Integer, Integer> seatsSpread;
    /**
     * Типы вагонов.
     */
    private SeatAttribute.CarriageType[] carriageTypes;
    /**
     * Подмененное текущее время.
     */
    private Instant fakeNow;
    /**
     * Seed графа для одинакового построения
     */
    private long graphSeed;

    public static MapConfiguration getDefaultConfiguration() {
        MapConfiguration config = new MapConfiguration();
        config.setTrainBrands(Arrays.asList(new TrainBrand("B1"),
                new TrainBrand("B2"), new TrainBrand("B3")));
        config.setTrainCategoryNames(Arrays.asList("C1", "C2", "C3"));
        config.setRailwayCarrierNames(Arrays.asList("RA", "RB", "RC"));
        config.setCarriagesSpread(Pair.of(3, 5));
        config.setCoupesSpread(Pair.of(5, 10));
        config.setSeatsSpread(Pair.of(0, 5));
        config.setCarriageTypes(SeatAttribute.CarriageType.values());
        config.setFakeNow(LocalDate.of(2016, 2, 2).atStartOfDay().toInstant(ZoneOffset.UTC));
        config.setGraphSeed(55551);
        return config;
    }
}

