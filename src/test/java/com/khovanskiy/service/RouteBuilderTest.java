package com.khovanskiy.service;

import com.khovanskiy.config.RouteBuilderConfig;
import com.khovanskiy.model.Path;
import com.khovanskiy.model.Point;
import com.khovanskiy.model.PointTimeInterval;
import com.khovanskiy.model.Ref;
import com.khovanskiy.model.ResultPresentation;
import com.khovanskiy.model.StationPoint;
import com.khovanskiy.model.runtime.RouteBuilderQuery;
import com.khovanskiy.runtime.DefaultRouteBuilderFilter;
import com.khovanskiy.runtime.DefaultRouteBuilderHandler;
import com.khovanskiy.runtime.DefaultRouteBuilderResponse;
import com.khovanskiy.runtime.RouteBuilder;
import com.khovanskiy.runtime.RouteBuilderY;
import com.khovanskiy.util.GeneratedMap;
import com.khovanskiy.util.InstantInterval;
import com.khovanskiy.util.MapConfiguration;
import com.khovanskiy.util.MapGenerator;
import com.khovanskiy.util.Now;
import com.khovanskiy.util.Visualizer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author victor
 */
@Slf4j
public class RouteBuilderTest {
    private final Repository repository = new Repository();

    private final TransportRunService transportRunService = new TransportRunService();

    private final RouteBuilderConfig config = new RouteBuilderConfig(3, 1800, 86400, 5, 500);

    private final RouteBuilder routeBuilder = new RouteBuilder(config, repository);

    /**
     * Количество потоков
     */
    public static final int THREADS_COUNT = 1;
    /**
     * Количество запросов (удачных)
     */
    public static final int QUERIES_COUNT = 100;
    /**
     * Seed графа для одинакового построения
     */
    public static final long GRAPH_SEED = 55551;
    public static final Random random = new Random(GRAPH_SEED);
    /**
     * Разрешение обновиться RouteBuilder
     */
    public static final int BUILDER_UPDATE = 1;
    /**
     * Разрешение выполнять запросы
     */
    public static final int BUILDER_FETCH = 3;
    /**
     * Разрешение проверять корректность результатов
     */
    public static final int TEST_RESULT = 7;
    /**
     * Разрешение визуализировать модель
     */
    public static final int VISUALIZE_MODEL = 16;
    /**
     * Текущие права
     */
    public static final int CURRENT_MODE = VISUALIZE_MODEL | BUILDER_FETCH;
    @Test
    public void main() {
        MapConfiguration configuration = MapConfiguration.getDefaultConfiguration();
        Now.setClock(Clock.fixed(configuration.getFakeNow(), ZoneId.systemDefault()));
        MapGenerator generator = new MapGenerator(configuration, repository, transportRunService);
        GeneratedMap map = generator.generate();

        if ((CURRENT_MODE & VISUALIZE_MODEL) == VISUALIZE_MODEL) {
            //RouteBuilderY routeBuilderY = new RouteBuilderY(repository);
            //routeBuilderY.allocateHubs(map.getStations());
            Visualizer.visualize(repository, "/tmp/model.dot");
        }

        if ((CURRENT_MODE & BUILDER_UPDATE) == BUILDER_UPDATE) {
            routeBuilder.update(map.getTrainRuns(), Collections.emptyList(), Collections.emptyList());

            List<RouteBuilderQuery> queries = new ArrayList<>();
            if ((CURRENT_MODE & BUILDER_FETCH) == BUILDER_FETCH) {
                //queries.add(manualQuery(configuration, new StationPoint.Id("4"), new StationPoint.Id("3"), 5));

                for (int i = 0; i < QUERIES_COUNT; ++i) {
                    RouteBuilderQuery query = generateQuery(configuration, map.getStations());
                    queries.add(query);
                }
            }
            List<TestResult> testResults = new ArrayList<>(queries.size());
            for (int i = 0; i < queries.size(); ++i) {
                try {
                    RouteBuilderQuery query = queries.get(i);
                    log.info((i + 1) + ") Запрос: " + query);
                    long prev = System.currentTimeMillis();
                    DefaultRouteBuilderResponse response = routeBuilder.findRoutes(query, new DefaultRouteBuilderHandler());
                    //List<Trip> trips = routeBuilder.find(query);
                    long next = System.currentTimeMillis();
                    if (response.getRoutes().size() > 0) {
                        testResults.add(new TestResult(response.getRoutes().size(), next - prev));
                        System.out.println(query);
                        for (Path path : response.getRoutes()) {
                            log.info(path + "");
                        }
                    } else {
                        //--i;
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    testResults.add(new TestResult(e));
                }
            }/**/
            if ((CURRENT_MODE & TEST_RESULT) == TEST_RESULT) {
                handleResult(testResults);
            }/**/
        }
    }

    @SuppressWarnings("unchecked")
    private RouteBuilderQuery generateQuery(MapConfiguration configuration, List<MapGenerator.GeoPoint> points) {
        MapGenerator.GeoPoint departurePoint = points.get(random.nextInt(points.size()));
        MapGenerator.GeoPoint arrivalPoint = points.get(random.nextInt(points.size()));
        Instant currentInstant = configuration.getFakeNow();
        ResultPresentation.SortOrder order = random.nextBoolean() ? ResultPresentation.SortOrder.DEPARTURE : ResultPresentation.SortOrder.ARRIVAL;
        ResultPresentation.SortDirection direction = random.nextBoolean() ? ResultPresentation.SortDirection.ASC : ResultPresentation.SortDirection.DESC;
        return new RouteBuilderQuery(
                new PointTimeInterval(departurePoint.getPoint().getId(), new InstantInterval(currentInstant, null)),
                new PointTimeInterval(arrivalPoint.getPoint().getId(), InstantInterval.full()),
                new DefaultRouteBuilderFilter(),
                new ResultPresentation(ResultPresentation.SortOrder.TRANSFERS, direction, 15),
                0
        );
    }

    @SuppressWarnings("unchecked")
    private RouteBuilderQuery manualQuery(MapConfiguration configuration, Ref<? extends Point> from, Ref<? extends Point> to, int k) {
        return new RouteBuilderQuery(
                new PointTimeInterval(from, InstantInterval.since(configuration.getFakeNow())),
                new PointTimeInterval(to, InstantInterval.full()),
                new DefaultRouteBuilderFilter(),
                new ResultPresentation(ResultPresentation.SortOrder.TRANSFERS, ResultPresentation.SortDirection.ASC, k),
                0);
    }

    /**
     * Обработка результатов тестирования
     */
    private void handleResult(List<TestResult> testResults) {
        log.info("Обработка результатов тестирования...");
        double E = 0.0;
        double E2 = 0.0;
        int totalCount = testResults.size();
        int validCount = 0;
        int invalidCount = 0;
        for (TestResult result : testResults) {
            if (result.isValid()) {
                E += result.duration;
                E2 += result.duration * result.duration;
                ++validCount;
            } else {
                ++invalidCount;
            }
        }
        E /= totalCount;
        E2 /= totalCount;
        double sigma = Math.sqrt(E2 - E * E);
        log.info("Успешно: " + validCount + ", неуспешно: " + invalidCount);
        log.info("Mean = " + E + ", Sigma = " + sigma);
    }

    private class TestResult {
        long duration = 0;
        int pathsCount = 0;
        Exception exception;

        public TestResult(int pathsCount, long duration) {
            this.pathsCount = pathsCount;
            this.duration = duration;
        }

        public TestResult(Exception e) {
            this.exception = e;
        }

        public boolean isValid() {
            return this.exception == null;
        }
    }
}
