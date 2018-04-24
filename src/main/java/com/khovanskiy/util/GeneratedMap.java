package com.khovanskiy.util;

import com.khovanskiy.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
public class GeneratedMap {
    public Instant fakeNow;
    public SeatAttribute.CarriageType[] carriageTypes;
    public CarriageType[] businessCarriageTypes;
    public List<TrainCategory> trainCategories;
    public List<Train> trains;
    public List<RailwayCarrier> railwayCarriers;
    public List<MapGenerator.GeoPoint> stations;
    public List<MapGenerator.GeoPoint> hubs;
    public List<TrainRun> trainRuns;
    public Random random;
}
