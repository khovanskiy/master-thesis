package com.khovanskiy.runtime;

import com.khovanskiy.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.comparators.ComparatorChain;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

/**
 * @author victor
 */
@Slf4j
public class Algorithm implements Iterable<Path> {

    private enum Stage {
        INITIAL,
        EXECUTED,
        PROPERTIES
    }

    private static final Comparator<PersistentState> COMPARATOR_BY_ARRIVAL_TIME = (lhs, rhs) -> lhs.segment.C().getTime().compareTo(rhs.segment.C().getTime());

    private static final Comparator<PersistentState> COMPARATOR_BY_DEPARTURE_TIME = (lhs, rhs) -> lhs.segment.A().getTime().compareTo(rhs.segment.A().getTime());

    private static final Comparator<PersistentState> COMPARATOR_BY_TRANSFERS_COUNT = (lhs, rhs) -> Integer.compare(lhs.transfers, rhs.transfers);

    private final Comparator<? super LazyLink> COMPARATOR_BY_TIME = (lhs, rhs) -> {
        assert lhs.state != null || lhs.segment != null : "Префикс и дополнительный сегмент не могут быть null одновременно";
        assert rhs.state != null || rhs.segment != null : "Префикс и дополнительный сегмент не могут быть null одновременно";
        PersistentState left = lhs.segment != null ? new PersistentState(lhs.state, lhs.segment, 0) : lhs.state;
        PersistentState right = rhs.segment != null ? new PersistentState(rhs.state, rhs.segment, 0) : rhs.state;
        return buildFrom(left, true).getTime().compareTo(buildFrom(right, true).getTime());
    };

    private final Set<Stop>[] expandedVertexes;
    private final Set<State<Stop>> exploredVertexes = new HashSet<>();
    private final Queue<State<Stop>> queue = new ArrayDeque<>();
    private final Map<Stop, Integer>[] pathsCount;
    private final Map<Stop, List<ForwardSegment>>[] successors;
    private final Map<Stop, List<ForwardSegment>>[] predecessors;

    private final Deque<Level> stack = new ArrayDeque<>();
    private final Set<String> usedTransportRunSets = new HashSet<>();
    private final StringBuilder transportRunSetBuilder = new StringBuilder();
    private final Queue<Path> candidates = new ArrayDeque<>();
    private final Set<Stop> sources = new HashSet<>();
    private final Set<Stop> targets = new HashSet<>();
    private final Map<Stop, List<PathWithCost>>[] allBestPaths;
    private final Map<Stop, Properties>[] allProperties;
    private final Map<Stop, Node>[] nodes;

    private final Set<Ref<? extends Point>> departures = new HashSet<>();
    private final Set<Ref<? extends Point>> arrivals = new HashSet<>();

    private final Transfers transfers;
    private final int maxTransfersCount;
    private final ResultPresentation.SortOrder order;
    private final ResultPresentation.SortDirection direction;

    private Stage stage = Stage.INITIAL;

    @Getter
    private Properties properties = Properties.empty();

    @SuppressWarnings("unchecked")
    public Algorithm(
            Transfers transfers,
            int maxTransfersCount,
            ResultPresentation.SortOrder order,
            ResultPresentation.SortDirection direction
    ) {
        this.transfers = transfers;
        this.maxTransfersCount = maxTransfersCount;
        this.order = order;
        this.direction = direction;
        this.expandedVertexes = new Set[this.maxTransfersCount + 1];
        this.pathsCount = new Map[this.maxTransfersCount + 1];
        this.successors = new Map[this.maxTransfersCount + 1];
        this.predecessors = new Map[this.maxTransfersCount + 1];
        this.allProperties = new Map[this.maxTransfersCount + 1];
        this.allBestPaths = new Map[this.maxTransfersCount + 1];
        this.nodes = new Map[this.maxTransfersCount + 1];
        for (int i = 0; i <= this.maxTransfersCount; ++i) {
            this.expandedVertexes[i] = new HashSet<>();
            this.pathsCount[i] = new HashMap<>();
            this.successors[i] = new HashMap<>();
            this.predecessors[i] = new HashMap<>();
            this.allProperties[i] = new HashMap<>();
            this.allBestPaths[i] = new HashMap<>();
            this.nodes[i] = new HashMap<>();
        }
    }

    /**
     * Добавить начальную точку
     *
     * @param source начальная точка
     */
    public void addSource(Stop source) {
        sources.add(source);
    }

    /**
     * Добавить конечную точку
     *
     * @param target конечная точка
     */
    public void addTarget(Stop target) {
        targets.add(target);
    }

    /**
     * Построение дерева путей
     */
    public Algorithm execute() {
        assert stage == Stage.INITIAL;
        sources.forEach(source -> {
            departures.add(source.getPoint());

            pathsCount[0].put(source, 1);
            State<Stop> sourceState = new State<>(source, 0);
            exploredVertexes.add(sourceState);
            queue.add(sourceState);
            //System.out.println("Добавили в очередь состояние = " + sourceState);
        });
        targets.forEach(target -> {
            arrivals.add(target.getPoint());
        });
        while (!queue.isEmpty()) {
            State<Stop> state = queue.poll();
            //System.out.println("Извлекли из очереди состояние = " + state);
            Stop current = state.vertex;
            int currentTransfersCount = state.transfersCount;

            expandedVertexes[currentTransfersCount].add(current);
            //System.out.println("Поместили остановку " + current + " в список обработанных c " + currentTransfersCount + " пересадкой");
            if (targets.contains(state.vertex)) {
                //System.out.println("Остановка " + current + " является конечной => переход к следующему состоянию");
                continue;
            }

            List<ForwardSegment> transfers = this.transfers.successors(departures, current, arrivals);
            assert pathsCount[currentTransfersCount].get(current) != null;
            /*if (transfers.isEmpty()) {
                System.out.println("Пересадок нет и прямых тоже");
            }*/
            //successors[currentTransfersCount].put(current, transfers);
            transfers.forEach(segment -> {
                // Теперь segment.from() != current, т.к. на текущей станции мы уже находимся в нужном поезде;
                assert segment.isTransfer() || (!segment.isTransfer() && segment.A().getNumber() < segment.C().getNumber());
                Stop neighbor = segment.C();
                //System.out.println("\tРассматриваем переход = " + segment);

                int newTransfersCount = currentTransfersCount;

                if (segment.isTransfer()) {
                    //System.out.println("\tЭто пересадка");
                    newTransfersCount += 1;
                    if (newTransfersCount > maxTransfersCount) {
                        //System.out.println("\tБольше пересадок не совершить, т.к. текущее количество пересадок = " + currentTransfersCount + ", а максимальное доспустимое = " + maxTransfersCount + " => к следующему");
                        return;
                    }
                } else {
                    //System.out.println("\tЭто не пересадка");
                }

                /*if (newBasisSize + transitionsHeuristic.estimate(neighbor, target) > maxTransfersCount) {
                    continue;
                }*/

                if (expandedVertexes[newTransfersCount].contains(neighbor)) {
                    //System.out.println("\tОстановка " + neighbor + " находится в списоке обработанных c " + newTransfersCount + " пересадкой => к следующему");
                    return;
                }

                List<ForwardSegment> cc = predecessors[newTransfersCount].get(neighbor);
                if (cc == null) {
                    cc = new ArrayList<>();
                    predecessors[newTransfersCount].put(neighbor, cc);
                }
                cc.add(segment);
                //System.out.println("\tДобавили обратное ребро = " + segment + " к " + neighbor + " с количеством пересадок = " + newTransfersCount);

                pathsCount[newTransfersCount].put(neighbor, pathsCount[currentTransfersCount].get(current) + pathsCount[newTransfersCount].getOrDefault(neighbor, 0));

                State<Stop> newState = new State<>(neighbor, newTransfersCount);
                if (!exploredVertexes.contains(newState)) {
                    //System.out.println("\tНовое состояние " + newState + " помещаем в очередь");
                    exploredVertexes.add(newState);
                    queue.add(newState);
                }
            });
        }

        stage = Stage.EXECUTED;

        log();

        switch (order) {
            case TIME:
                sortedByTime();
                break;
            case DEPARTURE:
                sortedByDepartureTime();
                break;
            case ARRIVAL:
                sortedByArrivalTime();
                break;
            case TRANSFERS:
            default:
                sortedByTransfersCount();
                break;
        }

        return this;
    }

    private void log() {
        log.info("Paths` count:");
        int total = 0;
        for (int i = 0; i <= maxTransfersCount; ++i) {
            int count = 0;
            for (Stop target : targets) {
                count += pathsCount[i].getOrDefault(target, 0);
            }
            if (i == 0) {
                log.info("No transfers = " + count);
            } else {
                log.info(i + " transfers  = " + count);
            }

            total += count;
        }
        log.info("Total count  = " + total);
        /**/
    }

    /**
     * Заполняем уровень сегментами, входящими в конечные точки.
     *
     * @param level          уровень
     * @param transfersCount количество пересадок
     */
    private void fillLevel(Level level, int transfersCount) {
        for (Stop target : targets) {
            Collection<ForwardSegment> neighbors = predecessors[transfersCount].getOrDefault(target, Collections.emptyList());
            for (ForwardSegment segment : neighbors) {
                assert segment.C().equals(target);
                level.add(new PersistentState(transfersCount, segment));
            }
        }
    }

    /**
     * Подготовка к построению маршрутов, отсортированных по количеству пересадок
     */
    public Algorithm sortedByTransfersCount() {
        assert stage == Stage.EXECUTED | stage == Stage.PROPERTIES;
        // Добавляем концы путей, ведущие в target, и считаем количество путей с любым количеством базисных ребер
        Level unvisited = new RandomLevel();
        // по построению уровень будет отсортирован по количеству пересадок
        if (direction == ResultPresentation.SortDirection.ASC) {
            for (int i = 0; i <= maxTransfersCount; ++i) {
                fillLevel(unvisited, i);
            }
        } else {
            for (int i = maxTransfersCount; i >= 0; --i) {
                fillLevel(unvisited, i);
            }
        }
        stack.addLast(unvisited);

        // форсируем поиск первого маршрута
        backwardNext();
        return this;
    }

    /**
     * Подготовка к построению маршрутов, отсортированных по времени прибытия
     */
    public Algorithm sortedByArrivalTime() {
        assert stage == Stage.EXECUTED | stage == Stage.PROPERTIES;
        ComparatorChain<PersistentState> comparatorChain = new ComparatorChain<>();
        comparatorChain.addComparator(COMPARATOR_BY_TRANSFERS_COUNT);
        // Добавляем концы путей, ведущие в target, и считаем количество путей с любым количеством базисных ребер
        Comparator<PersistentState> comparator = COMPARATOR_BY_ARRIVAL_TIME;
        if (direction == ResultPresentation.SortDirection.DESC) {
            comparator = comparator.reversed();
        }
        comparatorChain.addComparator(comparator);
        Level unvisited = new OrderedLevel(comparatorChain);
        for (int i = 0; i <= maxTransfersCount; ++i) {
            fillLevel(unvisited, i);
        }
        stack.addLast(unvisited);

        // форсируем поиск первого маршрута
        backwardNext();
        return this;
    }

    /**
     * Подготовка к построению маршрутов, отсортированных по времени отправления
     */
    public Algorithm sortedByDepartureTime() {
        assert stage == Stage.EXECUTED | stage == Stage.PROPERTIES;
        // требуется множество исходящих ребер, которые принадлежат маршрутам
        buildSuccessors();
        ComparatorChain<PersistentState> comparatorChain = new ComparatorChain<>();
        comparatorChain.addComparator(COMPARATOR_BY_TRANSFERS_COUNT);
        // добавлем начала путей, исходящих из source
        Comparator<PersistentState> comparator = COMPARATOR_BY_DEPARTURE_TIME;
        if (direction == ResultPresentation.SortDirection.DESC) {
            comparator = comparator.reversed();
        }
        comparatorChain.addComparator(comparator);
        Level unvisited = new OrderedLevel(comparatorChain);
        for (Stop source : sources) {
            Collection<ForwardSegment> neighbors = successors[0].getOrDefault(source, Collections.emptyList());
            for (ForwardSegment segment : neighbors) {
                assert segment.A().equals(source);
                // путь может состоянить из одного сегмента без пересадки или из нескольких сегментов
                unvisited.add(new PersistentState(segment.isTransfer() ? 1 : 0, segment));
            }
        }
        stack.addLast(unvisited);

        // форсируем поиск первого маршрута
        forwardNext();
        return this;
    }

    /**
     * Подготовка к построению маршрутов, сортированных по времени в пути
     */
    public Algorithm sortedByTime() {
        assert stage == Stage.EXECUTED | stage == Stage.PROPERTIES;

        Comparator<? super LazyLink> comparator = COMPARATOR_BY_TIME;
        if (direction == ResultPresentation.SortDirection.DESC) {
            comparator = comparator.reversed();
        }
        Node root = new Node(comparator);
        for (int i = 0; i <= maxTransfersCount; ++i) {
            for (Stop target : targets) {
                Node node = buildIteratorTree(target, i, comparator);
                Iterator<PersistentState> iterator = node.iterator();
                if (iterator.hasNext()) {
                    PersistentState state = iterator.next();
                    // если префикс до конечной остановки нулевой, то маршрута до этой остановки не существует
                    if (state != null) {
                        root.queue.add(new LazyLink(null, state, iterator));
                    }
                }
            }
        }
        metricIterator = root.iterator();

        return this;
    }

    private Iterator<PersistentState> metricIterator;

    /**
     * Строит и возвращает следующий путь в соответствии с заданной метрикой
     *
     * @return следующий путь
     */
    private Path metricNext() {
        if (!metricIterator.hasNext()) {
            throw new NoSuchElementException();
        }
        PersistentState state = metricIterator.next();
        return buildFrom(state, true);
    }

    private Node buildIteratorTree(Stop stop, int transfersCount, Comparator<? super LazyLink> comparator) {
        Node parent = nodes[transfersCount].get(stop);
        if (parent == null) {
            parent = new Node(comparator);
            nodes[transfersCount].put(stop, parent);
            List<ForwardSegment> incoming = predecessors[transfersCount].getOrDefault(stop, Collections.emptyList());
            for (ForwardSegment segment : incoming) {
                int prevTransfers = transfersCount - (segment.isTransfer() ? 1 : 0);
                Node child = buildIteratorTree(segment.A(), prevTransfers, comparator);
                parent.add(segment, child.iterator());
            }
        }
        return parent;
    }

    @Data
    private class LazyLink {
        /**
         * исходящее ребро от узла
         */
        private ForwardSegment segment;
        /**
         * последнее известное состояние узла
         */
        private PersistentState state;
        /**
         * итератор для получения следующего состояние узла
         */
        private Iterator<PersistentState> iterator;

        public LazyLink(ForwardSegment segment, PersistentState state, Iterator<PersistentState> iterator) {
            assert state != null || segment != null : "Префикс и дополнительный сегмент не могут быть null одновременно";
            this.segment = segment;
            this.state = state;
            this.iterator = iterator;
        }
    }

    private class Node implements Iterable<PersistentState> {
        private final int numberOfResult = 10;
        private PriorityQueue<LazyLink> queue;
        private List<PersistentState> cache = new ArrayList<>();

        public Node(Comparator<? super LazyLink> comparator) {
            this.queue = new PriorityQueue<>(comparator);
        }

        public void add(ForwardSegment segment, Iterator<PersistentState> iterator) {
            if (iterator.hasNext()) {
                PersistentState state = iterator.next();
                assert state != null || segment != null : "Префикс и дополнительный сегмент не могут быть null одновременно";
                queue.add(new LazyLink(segment, state, iterator));
            }
        }

        /**
         * @return минимальный префикс пути
         */
        private PersistentState nextState() {
            // извлекаем оптимальную по метрике связь
            LazyLink link = queue.poll();
            assert link != null;
            assert link.iterator != null;
            if (link.iterator.hasNext()) {
                // если существует еще префиксы, то получим оптимальный и обновим ссылку
                queue.add(new LazyLink(link.segment, link.iterator.next(), link.iterator));
            }
            // получаем оптимальный префикс пути
            PersistentState prevState = link.state;
            if (link.segment != null) {
                // если не являемся корнем, то дополняем префикс новым сегментом
                return new PersistentState(prevState, link.segment, 0);
            }
            return prevState;
        }

        @Override
        public Iterator<PersistentState> iterator() {
            if (queue.isEmpty()) {
                // очередь изначально пуста, следовательно это начальный узел
                return new Iterator<PersistentState>() {
                    private boolean flag;

                    @Override
                    public boolean hasNext() {
                        return !flag;
                    }

                    @Override
                    public PersistentState next() {
                        flag = true;
                        // поэтому префиксов до начального узла нет
                        return null;
                    }
                };
            } else {
                // очередь не пуста, следовательно есть префиксы пути, ведущие в текущую вершину
                return new Iterator<PersistentState>() {
                    private int index;

                    @Override
                    public boolean hasNext() {
                        return index < cache.size() || !queue.isEmpty();
                    }

                    @Override
                    public PersistentState next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        if (cache.size() <= index) {
                            cache.add(nextState());
                        }
                        ++index;
                        return cache.get(index - 1);
                    }
                };
            }
        }
    }

    /**
     * Построение свойств доступных маршрутов
     */
    public Algorithm buildProperties() {
        assert stage == Stage.EXECUTED;
        //todo build properties skipping useless paths

        properties = Properties.empty();
        for (int i = 0; i <= maxTransfersCount; ++i) {
            for (Stop target : targets) {
                properties = properties.max(buildProperties(target, i));
            }
        }

        stage = Stage.PROPERTIES;
        return this;
    }

    private Properties buildProperties(Stop stop, int transfersCount) {
        Properties properties = allProperties[transfersCount].get(stop);
        if (properties == null) {
            properties = Properties.empty();
            List<ForwardSegment> incoming = predecessors[transfersCount].getOrDefault(stop, Collections.emptyList());
            for (ForwardSegment segment : incoming) {
                int prevTransfers = transfersCount - (segment.isTransfer() ? 1 : 0);
                Stop previousStop = segment.A();
                properties = properties.max(buildProperties(previousStop, prevTransfers).min(segment.getProperties()));
            }
            allProperties[transfersCount].put(stop, properties);
        }
        return properties;
    }

    /**
     * Построение множества исходящих сегментов
     */
    public Algorithm buildSuccessors() {
        assert stage == Stage.EXECUTED || stage == Stage.PROPERTIES;

        for (int i = 0; i <= maxTransfersCount; ++i) {
            for (Stop target : targets) {
                buildSuccessors(target, i);
            }
        }

        return this;
    }

    private void buildSuccessors(Stop stop, int transfersCount) {
        List<ForwardSegment> incoming = predecessors[transfersCount].getOrDefault(stop, Collections.emptyList());
        for (ForwardSegment segment : incoming) {
            int prevTransfers = transfersCount - (segment.isTransfer() ? 1 : 0);
            assert prevTransfers >= 0;
            Stop previousStop = segment.A();
            assert segment.C().equals(stop);
            List<ForwardSegment> outgoing = successors[prevTransfers].get(previousStop);
            if (outgoing == null) {
                outgoing = new ArrayList<>();
                successors[prevTransfers].put(previousStop, outgoing);
            }
            outgoing.add(segment);
            buildSuccessors(previousStop, prevTransfers);
        }
    }

    /**
     * Построение {@code k} лучших маршрутов по аддитивной метрике
     *
     * @param k            количество маршрутов
     * @param costFunction функция для подсчета стоимости
     * @return список маршрутов
     */
    @Deprecated
    public List<Path> buildMetrics(int k, Function<ForwardSegment, Double> costFunction) {
        assert stage == Stage.EXECUTED | stage == Stage.PROPERTIES;

        List<PathWithCost> pathWithCosts = new ArrayList<>();
        for (int i = 0; i <= maxTransfersCount; i++) {
            for (Stop target : targets) {
                pathWithCosts.addAll(buildMetrics(target, i, k, costFunction));
            }
        }
        Collections.sort(pathWithCosts);
        List<Path> result = new ArrayList<>();
        int resultSize = Math.min(k, pathWithCosts.size());
        for (int i = 0; i < resultSize; i++) {
            result.add(pathWithCosts.get(i).getPath());
        }
        return result;
    }


    @Deprecated
    private List<PathWithCost> buildMetrics(Stop stop, int transfersCount, int k, Function<ForwardSegment, Double> costFunction) {
        List<PathWithCost> bestPaths = allBestPaths[transfersCount].get(stop);
        if (bestPaths == null) {
            bestPaths = new ArrayList<>();
            List<ForwardSegment> incoming = predecessors[transfersCount].getOrDefault(stop, Collections.emptyList());
            for (ForwardSegment segment : incoming) {
                int previousTransfers = transfersCount - (segment.isTransfer() ? 1 : 0);
                Stop previousStop = segment.A();
                List<PathWithCost> previousBestPaths = buildMetrics(previousStop, previousTransfers, k, costFunction);
                for (PathWithCost pathWithCost : previousBestPaths) {
                    pathWithCost.path.add(segment);
                    pathWithCost.cost += costFunction.apply(segment);
                }
            }
            Collections.sort(bestPaths);
            ArrayList<PathWithCost> result = new ArrayList<>();
            int resultSize = Math.min(k, bestPaths.size());
            for (int i = 0; i < resultSize; i++) {
                result.add(bestPaths.get(i));
            }
            allBestPaths[transfersCount].put(stop, result);
        }
        return bestPaths;
    }

    private interface Level {
        /**
         * Возвращает первое состояние (по порядку или сортировке) и убирает его из очереди.
         *
         * @return первое состояние
         */
        PersistentState poll();

        /**
         * Добавляет новое состояние на уровень.
         *
         * @param persistentState новое состояние
         * @return успешна ли операция
         */
        boolean add(PersistentState persistentState);

        /**
         * @return пустой ли уровень
         */
        boolean isEmpty();
    }

    /**
     * Упорядоченный слой DFS
     */
    private class OrderedLevel extends PriorityQueue<PersistentState> implements Level {

        public OrderedLevel(Comparator<? super PersistentState> comparator) {
            super(comparator);
        }

        @Override
        public PersistentState poll() {
            return super.poll();
        }
    }

    /**
     * Неупорядоченный слой DFS
     */
    private class RandomLevel extends ArrayDeque<PersistentState> implements Level {
        @Override
        public PersistentState poll() {
            return super.pollFirst();
        }

        @Override
        public boolean add(PersistentState persistentState) {
            super.addLast(persistentState);
            return true;
        }
    }

    /**
     * Восстановление пути по дереву состояний
     *
     * @param state состояние, из которого начинается восстановление
     * @return восстановленный путь
     */
    private Path buildFrom(PersistentState state, boolean reverse) {
        Path path = new Path();
        PersistentState current = state;
        while (current != null) {
            path.add(current.segment);
            current = current.parent;
        }
        if (reverse) {
            Collections.reverse(path);
        }
        return path;
    }

    /**
     * Получение списка используемых в пути TransportRun'ов в виде строки
     * @param state состояние, которому соответствует путь
     * @return строковое представление списка TransportRun
     */
    private String buildString(PersistentState state) {
        transportRunSetBuilder.setLength(0);
        PersistentState current = state;
        while (current != null) {
            transportRunSetBuilder.append(current.segment.A().getRef()).append(",");
            current = current.parent;
        }
        return transportRunSetBuilder.toString();
    }

    /**
     * @return существует ли следующий путь
     */
    private boolean hasNext() {
        return !candidates.isEmpty();
    }

    /**
     * Строит и возвращает следующий путь по множеству входящих сегментов
     *
     * @return следующий путь или null
     */
    private Path backwardNext() {
        assert stage == Stage.EXECUTED || stage == Stage.PROPERTIES;
        Path candidate = null;
        if (!candidates.isEmpty()) {
            candidate = candidates.poll();
        }
        while (!stack.isEmpty()) {
            Level level = stack.peekLast();
            //System.out.println("## Level# " + stack.size());
            if (!level.isEmpty()) {
                PersistentState state = level.poll();
                String transportRunSetString = buildString(state);
                if (usedTransportRunSets.contains(transportRunSetString)) {
                    continue;
                }
                Stop vertex = state.segment.A();
                int transfers = state.transfers;

                if (departures.contains(vertex.getPoint())) {
                    //assert transfers == 0;
                    usedTransportRunSets.add(transportRunSetString);
                    Path path = buildFrom(state, false);
                    candidates.add(path);
                    if (path.size() > 1) {
                        stack.pollLast();
                    }
                    break;
                }

                List<ForwardSegment> incoming = predecessors[transfers].get(vertex);
                if (incoming != null) {
                    Level newLevel = new RandomLevel();
                    Collections.sort(incoming, (fs1, fs2) -> Integer.compare(fs2.C().getNumber(), fs1.C().getNumber()));
                    incoming.forEach(segment -> {
                        //assert segment.to() == vertex;
                        int prevTransfers = transfers;
                        if (segment.isTransfer()) {
                            --prevTransfers;
                            assert prevTransfers >= 0;
                        }
                        PersistentState newState = new PersistentState(state, segment, prevTransfers);
                        String idHash = buildString(newState);
                        if (!usedTransportRunSets.contains(idHash)) {
                            newLevel.add(newState);
                        }
                    });
                    stack.addLast(newLevel);
                }
            } else {
                // на текущем уровне не осталось состояний, можно подняться выше
                stack.pollLast();
            }
        }
        return candidate;
    }

    /**
     * Строит и возвращает следующий маршрут по множеству исходящих сегментов
     *
     * @return следующий путь или null
     */
    private Path forwardNext() {
        assert stage == Stage.EXECUTED || stage == Stage.PROPERTIES;
        Path candidate = null;
        if (!candidates.isEmpty()) {
            candidate = candidates.poll();
        }
        while (!stack.isEmpty()) {
            Level level = stack.peekLast();
            if (!level.isEmpty()) {
                PersistentState state = level.poll();
                String transportRunSetString = buildString(state);
                if (usedTransportRunSets.contains(transportRunSetString)) {
                    continue;
                }/**/
                Stop vertex = state.segment.C();
                int transfers = state.transfers;

                if (arrivals.contains(vertex.getPoint())) {
                    Path path = buildFrom(state, true);
                    usedTransportRunSets.add(transportRunSetString);
                    candidates.add(path);
                    if (path.size() > 1) {
                        stack.pollLast();
                    }
                    break;
                }

                List<ForwardSegment> outgoing = successors[transfers].get(vertex);
                if (outgoing != null) {
                    Level newLevel = new RandomLevel();
                    Collections.sort(outgoing, (fs1, fs2) -> Integer.compare(fs1.B().getNumber(), fs2.B().getNumber()));
                    outgoing.forEach(segment -> {
                        //assert segment.to() == vertex;
                        int nextTransfers = transfers;
                        if (segment.isTransfer()) {
                            ++nextTransfers;
                            assert nextTransfers <= maxTransfersCount;
                        }
                        PersistentState newState = new PersistentState(state, segment, nextTransfers);
                        String newSetString = transportRunSetString + "," + segment.A().getRef();
                        if (!usedTransportRunSets.contains(newSetString)) {
                            newLevel.add(newState);
                        }
                    });
                    stack.addLast(newLevel);
                }
            } else {
                // на текущем уровне не осталось состояний, можно подняться выше
                stack.pollLast();
            }
        }
        return candidate;
    }

    @Override
    public Iterator<Path> iterator() {
        switch (order) {
            case TIME:
                return new Iterator<Path>() {
                    @Override
                    public boolean hasNext() {
                        return metricIterator.hasNext();
                    }

                    @Override
                    public Path next() {
                        return Algorithm.this.metricNext();
                    }
                };
            case DEPARTURE:
                return new Iterator<Path>() {
                    @Override
                    public boolean hasNext() {
                        return Algorithm.this.hasNext();
                    }

                    @Override
                    public Path next() {
                        return Algorithm.this.forwardNext();
                    }
                };
            case ARRIVAL:
                return new Iterator<Path>() {
                    @Override
                    public boolean hasNext() {
                        return Algorithm.this.hasNext();
                    }

                    @Override
                    public Path next() {
                        return Algorithm.this.backwardNext();
                    }
                };
            case TRANSFERS:
            default:
                return new Iterator<Path>() {
                    @Override
                    public boolean hasNext() {
                        return Algorithm.this.hasNext();
                    }

                    @Override
                    public Path next() {
                        return Algorithm.this.backwardNext();
                    }
                };
        }
    }

    private class PersistentState {
        private final ForwardSegment segment;
        private final int transfers;
        private PersistentState parent;

        public PersistentState(int transfers, ForwardSegment segment) {
            this.transfers = transfers;
            assert segment != null;
            this.segment = segment;
        }

        public PersistentState(PersistentState parent, ForwardSegment segment, int transfers) {
            this(transfers, segment);
            assert parent != null || segment != null : "Префикс и дополнительный сегмент не могут быть null одновременно";
            this.parent = parent;
        }
    }

    private class State<T> {
        Stop vertex;
        int transfersCount;

        public State(Stop source, int transfersCount) {
            vertex = source;
            this.transfersCount = transfersCount;
        }

        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof State) {
                State<Stop> other = (State<Stop>) o;
                // todo:
                return vertex.equals(other.vertex) && transfersCount == other.transfersCount;
            }
            return true;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * PRIME + vertex.hashCode();
            result = result * PRIME + transfersCount;
            return result;
        }

        public String toString() {
            return "State(vertex=" + this.vertex + ", transfersCount=" + this.transfersCount + ")";
        }
    }

    @Data
    private class PathWithCost implements Comparable<PathWithCost> {
        public Path path;
        public Double cost;

        @Override
        public int compareTo(PathWithCost o) {
            if (this.cost < o.cost) {
                return -1;
            } else if (this.cost.equals(o.cost)) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
