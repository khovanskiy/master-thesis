package com.khovanskiy.util;

import com.khovanskiy.model.*;
import com.khovanskiy.service.Repository;
import com.khovanskiy.service.TransportRunService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.distribution.EnumeratedDistribution;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author victor
 */
@Slf4j
public class MapGenerator {
    private final MapConfiguration config;

    private final Random random;

    private final Repository repository;

    private final TransportRunService transportRunService;

    private final IdxGen<CoupeCarriage.Seat> coupeSeatIdxGen = new IdxGen<>();

    private final IdxGen<OpenPlanCarriage.Seat> openPlanSeatIdxGen = new IdxGen<>();

    private final IdxGen<HighComfortCarriage.Seat> highComfortSeatIdxGen = new IdxGen<>();

    private final IdxGen<CoupeCarriage> coupeCarriageIdxGen = new IdxGen<>();

    private final IdxGen<OpenPlanCarriage> openPlanCarriageIdxGen = new IdxGen<>();

    private final IdxGen<HighComfortCarriage> highComfortCarriageIdxGen = new IdxGen<>();

    public MapGenerator(MapConfiguration config, Repository repository, TransportRunService transportRunService) {
        this.config = config;
        this.random = new Random(config.getGraphSeed());
        this.repository = repository;
        this.transportRunService = transportRunService;
    }

    public GeneratedMap generate() {
        Clock realNowClock = Now.getClock();
        Instant now = config.getFakeNow();
        Now.setClock(Clock.fixed(now, ZoneId.systemDefault()));

        SeatAttribute.CarriageType[] carriageTypes = config.getCarriageTypes();
        CarriageType[] businessCarriageTypes = new CarriageType[carriageTypes.length];
        for (int i = 0; i < carriageTypes.length; i++) {
            businessCarriageTypes[i] = new CarriageType(new CarriageType.Id(i + ""), i + "", carriageTypes[i].name());
        }

        List<TrainCategory> trainCategories = generateTrainCategories();
        List<Train> trains = generateTrains(trainCategories);

        List<RailwayCarrier> railwayCarriers = generateRailwayCarriers();

        List<GeoPoint> stations = generateStations();
        int maxNeighborsRadius = config.getMaxNeighboursRadius();
        int maxNeighborsCount = config.getMaxNeighboursCount();
        stations.forEach(p -> {
            p.neighbours = getInRadius(p.x, p.y, stations, maxNeighborsRadius, maxNeighborsCount);
        });
        savePoints(stations);

        List<GeoPoint> hubs = generateHubs(stations);
        savePoints(hubs);

        List<TrainRun> trainRuns = generateRuns(stations, trains, railwayCarriers, businessCarriageTypes);
        saveTrainRuns(trainRuns);
        updateRuns();

        Now.setClock(realNowClock);
        return new GeneratedMap(now, carriageTypes, businessCarriageTypes, trainCategories, trains, railwayCarriers,
                stations, hubs, trainRuns, random);
    }

    private int randomRange(Pair<Integer, Integer> range) {
        return random.nextInt(range.getRight() - range.getLeft() + 1) + range.getLeft();
    }

    /**
     * Генерация хабов
     */
    private List<GeoPoint> generateHubs(List<GeoPoint> points) {
        log.info("Генерация " + config.getHubsCount() + " хабов...");
        List<GeoPoint> list = new ArrayList<>();
        for (int i = 0; i < config.getHubsCount(); ++i) {
            int hubSize = randomRange(config.getHubSizeSpread());
            String name = nextName(i);
            int x = randomRange(config.getSpreadX());
            int y = randomRange(config.getSpreadY());
            List<GeoPoint> geoPoints = getInRadius(x, y, points, config.getMaxHubsRadius(), hubSize);
            List<Ref<? extends Point>> refs = new ArrayList<>();
            for (GeoPoint geoPoint : geoPoints) {
                refs.add(geoPoint.getPoint().getId());
            }
            CityPoint point = new CityPoint("hub_" + i + "", name, ZoneId.systemDefault(), refs);
            GeoPoint geoPoint = new GeoPoint(point, x, y);
            log.info((i + 1) + ") " + geoPoint);
            list.add(geoPoint);
        }
        return list;
    }

    /**
     * Генерация ж/д станций
     */
    private List<GeoPoint> generateStations() {
        log.info("Генерация " + config.getPointsCount() + " станций...");
        List<GeoPoint> list = new ArrayList<>(config.getPointsCount());
        for (int i = 0; i < config.getPointsCount(); ++i) {
            String name = nextName(i);
            StationPoint point = new StationPoint(i + "", name, ZoneId.systemDefault());
            point.setState(State.ACTIVE);
            int x = randomRange(config.getSpreadX());
            int y = randomRange(config.getSpreadY());
            GeoPoint geoPoint = new GeoPoint(point, x, y);
            list.add(geoPoint);
            log.info((i + 1) + ") " + geoPoint);
        }
        return list;
    }

    private List<GeoPoint> getInRadius(int x, int y, List<GeoPoint> points, int radius, int limit) {
        PriorityQueue<GeoPoint> neighbours = new PriorityQueue<>((lhs, rhs) -> {
            return Double.compare(distance(x, y, lhs.x, lhs.y), distance(x, y, rhs.x, rhs.y));
        });
        points.forEach(p -> {
            double distance = distance(x, y, p.x, p.y);
            if (distance > 0 && distance < radius) {
                neighbours.add(p);
            }
        });
        List<GeoPoint> result = new ArrayList<>(limit);
        int count = 0;
        while (count < limit && !neighbours.isEmpty()) {
            result.add(neighbours.poll());
            ++count;
        }
        return result;
    }

    private void saveTrainRuns(List<TrainRun> runs) {
        log.info("Сохранение " + runs.size() + " маршрутов в репозиторий...");
        runs.forEach(repository::create);
        /*runs.forEach(model -> {
            repository.create(model);
            List<DateTrainRunEntry.KeyDay> ks = DateTrainRunEntry.getKeys(model.getDepartureTime(), model.getArrivalTime());
            ks.stream().forEach(key -> {
                FunFuture<DateTrainRunEntry> ff = dayTrainRun.find(key);
                if (ff == null || ff.get() == null) {
                    DateTrainRunEntry entry = new DateTrainRunEntry(key, 0, new ArrayList<>());
                    entry.getRefs().add(model.getId());
                    dayTrainRun.create(entry).get();
                } else {
                    DateTrainRunEntry entry = ff.get();
                    entry.getRefs().add(model.getId());
                    dayTrainRun.update(entry).get();
                }
            });
        });*/
    }

    private void savePoints(List<GeoPoint> points) {
        log.info("Сохранение " + points.size() + " точек в репозиторий...");
        points.forEach(p -> repository.create(p.getPoint()));
    }

    private <T> T randomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private List<RailwayCarrier> generateRailwayCarriers() {
        List<RailwayCarrier> railwayCarriers = new ArrayList<>();
        for (String railwayCarrierName : config.getRailwayCarrierNames()) {
            RailwayCarrier railwayCarrier = new RailwayCarrier();
            railwayCarrier.setState(State.ACTIVE);
            railwayCarrier.setFullName(railwayCarrierName);
            railwayCarrier.setId(new RailwayCarrier.Id(railwayCarrierName));
            railwayCarriers.add(railwayCarrier);
        }
        return railwayCarriers;
    }

    private List<TrainCategory> generateTrainCategories() {
        List<TrainCategory> trainCategories = new ArrayList<>();
        for (String trainCategoryName : config.getTrainCategoryNames()) {
            TrainCategory trainCategory = new TrainCategory(trainCategoryName, 1, 5);
            trainCategory.setState(State.ACTIVE);
            trainCategories.add(trainCategory);
            return trainCategories;
        }
        return trainCategories;
    }

    private List<Train> generateTrains(List<TrainCategory> trainCategories) {
        List<Train> trains = new ArrayList<>(config.getMaxTrainsCount());
        for (int i = 0; i < config.getMaxTrainsCount(); ++i) {
            Train train = new Train(
                    TypeOfEngine.LOCO,
                    new TrainNumber("T00" + i),
                    trainCategories.get(random.nextInt(trainCategories.size())),
                    config.getTrainBrands().get(random.nextInt(config.getTrainBrands().size()))
            );
            trains.add(train);
        }
        return trains;
    }

    private Carriage generateCarriage(int carriageNumber, CarriageType[] businessCarriageTypes) {
        SeatAttribute.CarriageType[] carriageTypes = config.getCarriageTypes();
        int carriageTypeNumber = random.nextInt(carriageTypes.length);
        SeatAttribute.CarriageType carriageType = carriageTypes[carriageTypeNumber];
        int seatNumber = randomRange(config.getSeatsSpread());
        int coupeNumber = randomRange(config.getCoupesSpread());
        switch (carriageType) {
            case COUPE: {
                Idx<CoupeCarriage> idx = coupeCarriageIdxGen.get();
                List<CoupeCarriage.Coupe> coupes = new ArrayList<>();
                for (int i = 0; i < coupeNumber; i++) {
                    List<CoupeCarriage.Seat> seats = new ArrayList<>();
                    for (int j = 0; j < seatNumber; j++) {
                        seats.add(new CoupeCarriage.Seat(coupeSeatIdxGen.get(), "Seat " + i, idx,
                                "Carriage " + carriageNumber));
                    }
                    coupes.add(new CoupeCarriage.Coupe(seats.size(), seats));
                }
                return new CoupeCarriage(idx, "T" + idx.getNumber(), businessCarriageTypes[carriageTypeNumber], coupes);
            }
            case HIGH_COMFORT: {
                Idx<HighComfortCarriage> idx = highComfortCarriageIdxGen.get();
                List<HighComfortCarriage.Coupe> coupes = new ArrayList<>();
                for (int i = 0; i < coupeNumber; i++) {
                    List<HighComfortCarriage.Seat> seats = new ArrayList<>();
                    for (int j = 0; j < seatNumber; j++) {
                        seats.add(new HighComfortCarriage.Seat(highComfortSeatIdxGen.get(), "Seat " + i, idx,
                                "Carriage " + carriageNumber));
                    }
                    coupes.add(new HighComfortCarriage.Coupe(seats.size(), seats));
                }
                return new HighComfortCarriage(idx, "T" + idx.getNumber(), businessCarriageTypes[carriageTypeNumber], coupes);
            }
            case OPEN_PLAN:
            default: {
                Idx<OpenPlanCarriage> idx = openPlanCarriageIdxGen.get();
                List<OpenPlanCarriage.Coupe> coupes = new ArrayList<>();
                for (int i = 0; i < coupeNumber; i++) {
                    List<OpenPlanCarriage.Seat> seats = new ArrayList<>();
                    for (int j = 0; j < seatNumber; j++) {
                        seats.add(new OpenPlanCarriage.Seat(openPlanSeatIdxGen.get(), "Seat " + i, idx,
                                "Carriage " + carriageNumber));
                    }
                    coupes.add(new OpenPlanCarriage.Coupe(seats.size(), seats));
                }
                return new OpenPlanCarriage(idx, "T" + idx.getNumber(), businessCarriageTypes[carriageTypeNumber], coupes);
            }
        }
    }

    private List<Carriage> generateCarriages(CarriageType[] businessCarriageTypes) {
        List<Carriage> carriages = new ArrayList<>();
        int carriageNumber = randomRange(config.getCarriagesSpread());
        for (int i = 0; i < carriageNumber; i++) {
            carriages.add(generateCarriage(i, businessCarriageTypes));
        }
        return carriages;
    }

    private List<TrainRun> generateRuns(List<GeoPoint> points, List<Train> trains, List<RailwayCarrier> railwayCarriers,
                                        CarriageType[] businessCarriageTypes) {
        int maxRunsCount = config.getMaxRunsCount();
        log.info("Генерация " + maxRunsCount + " уникальных маршрутов...");
        List<TrainRun> runs = new ArrayList<>();
        for (int i = 0; i < maxRunsCount; ++i) {
            Train train = trains.get(i % trains.size());
            RailwayRun.CarryingTrain carryingTrain = new RailwayRun.CarryingTrain(train, random.nextBoolean());
            int count = randomRange(config.getPointsInRunSpread());
            List<RailwayWaypoint> waypoints = new ArrayList<>();
            GeoPoint currentPoint = points.get(random.nextInt(points.size()));
            double distance = 0;
            Instant currentInstant = config.getFakeNow();
            Set<GeoPoint> visited = new HashSet<>();
            visited.add(currentPoint);

            IdxGen<RailwayWaypoint> waypointIdxGen = new IdxGen<>();
            for (int j = 0; j < count; ++j) {
                Instant arrTime;
                if (j == 0 || j == count - 1) {
                    arrTime = currentInstant;
                } else {
                    int waitingTime = randomRange(config.getTrainWaitingSpread());
                    arrTime = currentInstant;
                    currentInstant = currentInstant.plus(Duration.ofMinutes(waitingTime));
                }
                RailwayWaypoint wp = new RailwayWaypoint(
                        waypointIdxGen.get(),
                        currentPoint.getPoint().getId(),
                        arrTime,
                        currentInstant,
                        carryingTrain,
                        distance);
                waypoints.add(wp);

                EnumeratedDistribution<GeoPoint> neighbours;
                final GeoPoint fCurrentPoint = currentPoint;
                List<org.apache.commons.math3.util.Pair<GeoPoint, Double>> list = currentPoint.neighbours.stream()
                        .filter(n -> !visited.contains(n))
                        .map(q -> new org.apache.commons.math3.util.Pair<>(q, 1.0 / distance(fCurrentPoint, q)))
                        .collect(Collectors.toList());
                if (list.isEmpty()) {
                    break;
                }
                neighbours = new EnumeratedDistribution<>(list);
                //Collections.shuffle(neighbours, random);
                GeoPoint next = neighbours.sample();
                /*for (GeoPoint neighbour : neighbours) {
                    if (!visited.contains(neighbour)) {
                        visited.add(neighbour);
                        next = neighbour;
                        break;
                    }
                }*/
                if (next == null) {
                    break;
                }
                double segmentDistance = distance(currentPoint, next);
                double segmentTime = segmentDistance;
                distance += distance(currentPoint, next);
                currentPoint = next;

                currentInstant = currentInstant.plus(Duration.ofMinutes((long) segmentTime));
            }
            RailwayCarrier railwayCarrier = railwayCarriers.get(random.nextInt(railwayCarriers.size()));
            TrainRun run = new TrainRun(new TrainRun.Id("TrainRun" + i), "TrainRun" + i,
                    waypoints);
            run.setState(State.ACTIVE);
            List<Carriage> carriages = generateCarriages(businessCarriageTypes);
            run.fillRailwayRun(run, railwayCarrier.getId(), 45, carriages);
            runs.add(run);

            /*log.info("Маршрут " + run + " ");
            for (int j = 0; j < run.getWaypoints().size(); ++j) {
                RailwayWaypoint w = run.getWaypoints().get(j);
                //log.info("\t" + (j + 1) + ") W[train=" + w.getDepartureTrain() + ", point=" + w.getPoint() + ", dep=" + w.getDeparture() + ", arr=" + w.getArrival() + "]");
            }/**/
            //transportRunService.updateProperties(run);
        }
        return runs;
    }

    private void updateRuns() {
        List<TrainRun> runs = repository.findAll(TrainRun.class);
        log.info("Построение пересадок для " + runs.size() + " маршрутов...");
        for (int i = 0; i < runs.size(); ++i) {
            //transportRunService.updateTransfers(runs.get(i));
            log.info("Построено " + (i + 1) + "/" + runs.size());
        }
        log.info("Построение свойств для " + runs.size() + " маршрутов...");

        runs.forEach(run -> {
            transportRunService.updateProperties(run);
        });
    }

    private String nextName(int i) {
        return UUID.randomUUID().toString().substring(0, 6);
        //return Long.toString(System.currentTimeMillis() + i, Character.MAX_RADIX);
    }

    private double distance(GeoPoint lhs, GeoPoint rhs) {
        return Math.sqrt(Math.pow(lhs.x - rhs.x, 2) + Math.pow(lhs.y - rhs.y, 2));
    }

    private double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static class GeoPoint {
        Point point;
        @Getter
        int x;
        @Getter
        int y;
        List<GeoPoint> neighbours = new ArrayList<>();

        public GeoPoint(Point point, int x, int y) {
            this.point = point;
            this.x = x;
            this.y = y;
        }

        public Point getPoint() {
            return this.point;
        }

        public void setPoint(Point point) {
            this.point = point;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof GeoPoint) {
                GeoPoint other = (GeoPoint) o;
                return this.point == other.point && this.x == other.x && this.y == other.y;
            }
            return false;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + point.hashCode();
            result = result * PRIME + x;
            result = result * PRIME + y;
            return result;
        }

        public String toString() {
            return "GeoPoint(point=" + this.point + ", x=" + this.x + ", y=" + this.y + ")";//, neighbours=" + this.neighbours + ")";
        }
    }
}
