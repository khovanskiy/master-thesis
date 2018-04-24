package com.khovanskiy.runtime;

import com.khovanskiy.config.RouteBuilderConfig;
import com.khovanskiy.exception.InvalidNumberOfResultException;
import com.khovanskiy.model.*;
import com.khovanskiy.model.runtime.RouteBuilderFilter;
import com.khovanskiy.model.runtime.RouteBuilderQuery;
import com.khovanskiy.model.runtime.RouteBuilderResponse;
import com.khovanskiy.model.runtime.RouteBuilderResponseHandler;
import com.khovanskiy.service.Repository;
import com.khovanskiy.util.InstantInterval;
import com.khovanskiy.util.LRUCache;
import com.khovanskiy.util.Now;
import com.khovanskiy.util.SegmentTree;
import com.khovanskiy.util.Timeline;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author victor
 */
@Slf4j
public class RouteBuilder {
    private final RouteBuilderConfig config;

    /**
     * Кеш запросов текущего RouteBuilder
     */
    private LRUCache<Integer, Algorithm> requests;

    private final Repository repository;

    public RouteBuilder(RouteBuilderConfig config, Repository repository) {
        this.config = config;
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    public void update(List<? extends TransportRun> added, List<? extends TransportRun> updated, List<? extends TransportRun> deleted) {
        for (TransportRun transportRun : added) {
            List<? extends Waypoint> waypoints = transportRun.getWaypoints();
            for (int i = 0; i < waypoints.size(); ++i) {
                Waypoint w = waypoints.get(i);
                if (i != 0 && i != waypoints.size() - 1 && w.isNullStop()) {
                    continue;
                }
                //Schedule schedule;
                //repository.find(new SchedulePage.Id(w.getPoint().toString(), getDayOfInstant(w.getDeparture()).))
                /*synchronized (schedules) {
                    schedule = schedules.computeIfAbsent(w.getPoint(), pointRef -> new Schedule());
                }
                synchronized (schedule) {
                    StopLight stop = new StopLight(i, transportRun.getId());
                    SchedulePage schedulePage = schedule.computeIfAbsent(getDayOfInstant(w.getDeparture()), instant -> new SchedulePage());
                */
                assert w.getDeparture() != null;
                Ref<SchedulePage> schedulePageId = new SchedulePage.Id(w.getPoint().toString(), getDayOfInstant(w.getDeparture()));
                SchedulePage schedulePage = repository.find(schedulePageId).orElseGet(() -> {
                    SchedulePage var = new SchedulePage();
                    var.setId(schedulePageId);
                    repository.create(var);
                    return var;
                });
                StopLight stop = new StopLight(i, transportRun.getId());
                if (i != waypoints.size() - 1) {
                    schedulePage.getDepartureTimeline().put(w.getDeparture(), stop);
                }
                if (i != 0) {
                    schedulePage.getArrivalTimeline().put(w.getArrival(), stop);
                }
                repository.create(schedulePage);
                //}
            }
        }
        log.info("");
    }

    private long getDayOfInstant(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli();
    }

    /*
     * Получение списка остановок, попадающих в данный интервал времени
     *
     * @param timeline временная шкала с полным списоком остановок
     * @param interval данный интервал времени
     * @return множество подходящих остановок
     */
    /*private Timeline<Instant, Stop> subInterval(Timeline<Instant, Stop> timeline, InstantInterval interval) {
        if (interval.getSince() == null && interval.getTill() == null) {
            return timeline;
        } else if (interval.getSince() == null) {
            return timeline.headMap(interval.getTill(), true);
        } else if (interval.getTill() == null) {
            return timeline.tailMap(interval.getSince(), true);
        }
        return timeline.subMap(interval.getSince(), true, interval.getTill(), true);
    }*/

    private List<SchedulePage> fetchPages(Ref<? extends Point> ref, Instant from, Instant to) {
        List<SchedulePage> pages = new ArrayList<>(2);
        while (from.compareTo(to) <= 0) {
            repository.find(new SchedulePage.Id(ref.toString(), getDayOfInstant(from))).ifPresent(pages::add);
            from = from.plus(Duration.ofDays(1));
        }
        /*// todo: доделать для длительных интервалов
        long date1 = getDayOfInstant(from);
        repository.find(new SchedulePage.Id(ref.toString(), date1)).ifPresent(pages::add);
        long date2 = getDayOfInstant(to);
        if (date1 != date2) {
            repository.find(new SchedulePage.Id(ref.toString(), date2)).ifPresent(pages::add);
        }*/
        return pages;
    }

    @SuppressWarnings("unchecked")
    public List<ForwardSegment> successors(Collection<Ref<? extends Point>> departures, Stop A,
                                           Collection<Ref<? extends Point>> arrivals, Predicate<Properties> propertiesFilter) {
        TransportRun model = (TransportRun) repository.find(A.getRef()).get();

        List<ForwardSegment> segments = new ArrayList<>();
        SegmentTree<Properties> modelProperties = model.getProperties();

        // Проверка, можно ли доехать на текущем транспорте без пересадки
        List<Waypoint> waypoints = model.getWaypoints();
        int sourceId = waypoints.size();
        int targetId = waypoints.size();
        Set<Ref> visitedPoints = new HashSet<>();
        for (int i = A.getNumber() + 1; i < sourceId && i < targetId; ++i) {
            Waypoint w = waypoints.get(i);
            if (i != 0 && i != waypoints.size() - 1 && w.isNullStop()) {
                continue;
            }
            visitedPoints.add(w.getPoint());
            Stop idle = new Stop(i, A.getRef(), w.getPoint(), w.getArrival());//todo:
            // если можно доехать без пересадки, то игнорируем все пересадки на станции назначения
            if (departures.contains(idle.getPoint())) {
                // мы не хотим делать цикл через точку отправления
                sourceId = i;
                break;
            } else if (arrivals.contains(idle.getPoint())) {
                Properties properties = modelProperties.select(A.getNumber(), i);
                if (propertiesFilter.test(properties)) {
                    segments.add(new ForwardSegment(A, idle, idle, properties));
                }
                // будем считать, что нет петель или свойства аддитивны
                targetId = i;
                break;
            }

            Instant minTime = w.getArrival().plus(Duration.ofSeconds(config.getMinTransferTime()));
            Instant maxTime = w.getArrival().plus(Duration.ofSeconds(config.getMaxTransferTime()));
            List<SchedulePage> pages = fetchPages(w.getPoint(), minTime, maxTime);
            Stop B = new Stop(i, A.getRef(), w.getPoint(), w.getArrival());
            for (SchedulePage page : pages) {
                Timeline<StopLight> timeline = page.getDepartureTimeline().subMap(minTime, true, maxTime, true);
                for (Timeline.Entry<StopLight> entry : timeline) {
                    StopLight stopLight = entry.getValue();
                    if (stopLight.getNumber() == 0 || !stopLight.getRef().equals(A.getRef())) {
                        // Точка остановки следующего транспорта на последующей станции
                        Stop C = new Stop(stopLight.getNumber(), stopLight.getRef(), w.getPoint(), entry.getKey());
                        // Недопустим цикл на одной линии (движение прямо на предыдущую станцию движения)
                        // В такой реализации бьёт по производительности,
                        Ref nextRef = C.getRef();
                        TransportRun nextModel = (TransportRun) repository.find(nextRef).get();
                        List<Waypoint> nextWaypoints = nextModel.getWaypoints();
                        int nextNumber = C.getNumber() + 1;
                        boolean backwardDirection = false;
                        while (nextNumber < nextWaypoints.size()) {
                            while (nextNumber < nextWaypoints.size() - 1 && nextWaypoints.get(nextNumber).isNullStop()) {
                                ++nextNumber;
                            }
                            if (visitedPoints.contains(nextWaypoints.get(nextNumber).getPoint())) {
                                backwardDirection = true;
                                break;
                            }
                            ++nextNumber;
                        }
                        if (backwardDirection) {
                            continue;
                        }
                        // Свойства отрезка пути, по которому должен пройти текущий транспорт, чтобы можно было сделать пересадку
                        Properties properties = modelProperties.select(A.getNumber(), i);
                        if (propertiesFilter.test(properties)) {
                            segments.add(new ForwardSegment(A, B, C, properties));
                        }
                    }
                }
            }
        }

        return segments;
    }

    /**
     * Получение экземпляра алгоритма для построения маршрутов.
     * Если уже существует экземпляр с номеров запроса {@code query.getRequestId()}, то возвращается он, иначе создается новый.
     *
     * @param query   запрос
     * @param handler обработчик результатов выполнения алгоритма
     * @return экземпляр алгоритма
     */
    private <Q extends RouteBuilderQuery<Q, F, S>, R extends RouteBuilderResponse<S, F>, F extends RouteBuilderFilter<S>, S>
    Optional<Algorithm> getAlgorithm(Q query, RouteBuilderResponseHandler<R, S, F> handler) {
        if (query.getDeparture().getPoint().equals(query.getArrival().getPoint())) {
            return Optional.empty();
        }

        Instant now = Now.instant();
        InstantInterval intervalDeparture = query.getDeparture().getTimeInterval();
        if (intervalDeparture.getSince() == null) {
            intervalDeparture = intervalDeparture.withSince(Now.instant());
        } else if (intervalDeparture.getSince().compareTo(now) < 0) {
            intervalDeparture = intervalDeparture.withSince(Now.instant());
        }
        InstantInterval intervalArrival = query.getArrival().getTimeInterval();
        if (intervalArrival.getTill() == null) {
            intervalArrival = intervalArrival.withTill(Now.instant().plus(Duration.ofDays(45)));
        }
        if (intervalDeparture.getTill() == null) {
            intervalDeparture = intervalDeparture.withTill(intervalArrival.getTill());
        }
        if (intervalArrival.getSince() == null) {
            intervalArrival = intervalArrival.withSince(intervalDeparture.getSince());
        }
        assert intervalDeparture.getSince() != null && intervalDeparture.getTill() != null;
        assert intervalArrival.getSince() != null && intervalArrival.getTill() != null;

        Properties.PropertiesFilter propertiesFilter = handler.handleFilter(query.getFilter());
        Algorithm algorithm = new Algorithm(
                (departures1, A, arrivals1) -> {
                    List<ForwardSegment> newVersion = successors(departures1, A, arrivals1, propertiesFilter);
                    return newVersion;
                },
                propertiesFilter.getMaxTransfers(),
                query.getResultPresentation().getSortOrder(),
                query.getResultPresentation().getSortDirection()
        );
        /*List<Stop> sources1 = new ArrayList<>();
        for (Map.Entry<Instant, Stop> entry : departures) {
            sources1.add(entry.getValue());
        }*/
        List<Stop> sources2 = new ArrayList<>();
        for (SchedulePage page : fetchPages(query.getDeparture().getPoint(), intervalDeparture.getSince(), intervalDeparture.getTill())) {
            Timeline<StopLight> timeline = page.getDepartureTimeline().subMap(intervalDeparture.getSince(), true, intervalDeparture.getTill(), true);
            for (Timeline.Entry<StopLight> entry : timeline) {
                sources2.add(new Stop(entry.getValue().getNumber(), entry.getValue().getRef(), query.getDeparture().getPoint(), entry.getKey()));
            }
        }
        //assert sources1.size() == sources2.size() : sources1 + "\n" + sources2;
        if (sources2.isEmpty()) {
            return Optional.empty();
        }
        sources2.forEach(algorithm::addSource);

        /*List<Stop> targets1 = new ArrayList<>();
        for (Map.Entry<Instant, Stop> entry : arrivals) {
            targets1.add(entry.getValue());
        }*/
        List<Stop> targets2 = new ArrayList<>();
        for (SchedulePage page : fetchPages(query.getArrival().getPoint(), intervalArrival.getSince(), intervalArrival.getTill())) {
            Timeline<StopLight> timeline = page.getArrivalTimeline().subMap(intervalArrival.getSince(), true, intervalArrival.getTill(), true);
            for (Timeline.Entry<StopLight> entry : timeline) {
                targets2.add(new Stop(entry.getValue().getNumber(), entry.getValue().getRef(), query.getArrival().getPoint(), entry.getKey()));
            }
        }
        //assert targets1.size() == targets2.size() : targets1 + "\n" + targets2;
        if (targets2.isEmpty()) {
            return Optional.empty();
        }
        targets2.forEach(algorithm::addTarget);
        return Optional.of(algorithm);
    }

    /**
     * Финальное построение маршрутов
     *
     * @param algorithm       текущий алгоритм
     * @param numberOfResults требуемое количество маршрутов
     * @param handler         обработчик результатов выполнения алгоритма
     * @return список полученных маршрутов
     */
    private <R extends RouteBuilderResponse<S, F>, F extends RouteBuilderFilter<S>, S> List<S> buildRoutes(Algorithm algorithm, int numberOfResults, RouteBuilderResponseHandler<R, S, F> handler) {
        List<S> offers = new ArrayList<>();
        int found = 0;
        Iterator<Path> pathIterator = algorithm.iterator();
        Iterator<S> offerIterator = handler.getHandlingIterator(pathIterator);
        while (found < numberOfResults && offerIterator.hasNext()) {
            offers.add(offerIterator.next());
            ++found;
        }
        return offers;
    }

    public <Q extends RouteBuilderQuery<Q, F, S>, R extends RouteBuilderResponse<S, F>, F extends RouteBuilderFilter<S>, S> R findRoutes(Q query, RouteBuilderResponseHandler<R, S, F> handler) {
        if (query.getResultPresentation().getNumberOfResult() < 0) {
            throw new InvalidNumberOfResultException("number of results can not be less than 0");
        }
        int maxNumberOfResult = config.getMaxNumberOfResult();
        if (query.getResultPresentation().getNumberOfResult() > maxNumberOfResult && query.getFilter().getMaxTransfersCount() > 0) {
            throw new InvalidNumberOfResultException("current max number of results = " + maxNumberOfResult
                    + ", so " + query.getResultPresentation().getNumberOfResult() + " > " + maxNumberOfResult);
        }
        if (query.getResultPresentation().getNumberOfResult() == 0) {
            return handler.defaultResponse(Collections.emptyList(), Properties.empty(), 0);
        }
        try {
            log.info("RequestId = " + query.getRequestId());
            log.info("Thread = " + Thread.currentThread());
            //Visualizer.visualize(repository, "/tmp/fetch.dot");
            Optional<Algorithm> optionalAlgorithm = getAlgorithm(query, handler);
            if (optionalAlgorithm.isPresent()) {
                Algorithm algorithm = optionalAlgorithm.get();
                algorithm = algorithm.execute();

                F newFilter = query.getFilter();
                if (handler.isEmpty(newFilter)) {
                    algorithm.buildProperties();
                    newFilter = handler.handleProperties(algorithm.getProperties(), newFilter.getMaxTransfersCount(), newFilter.getRequiredQuantity());
                }/**/

                int required = query.getResultPresentation().getNumberOfResult();
                return handler.handleResponse(buildRoutes(algorithm, required, handler), newFilter, 0);
            } else {
                return handler.handleResponse(Collections.emptyList(), query.getFilter(), 0);
            }
        } catch (Exception e) {
            log.error("RouteBuilderException", e);
            return handler.defaultResponse(Collections.emptyList(), Properties.empty(), 0);
        }
    }

    @PostConstruct
    private void prepare() {
        requests = new LRUCache<>(config.getMaxCacheSize());
    }
}
