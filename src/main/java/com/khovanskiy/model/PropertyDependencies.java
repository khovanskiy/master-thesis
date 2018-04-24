package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * victor
 */
@Data
public class PropertyDependencies {
    public static final String ANY_VALUE = "ANY_VALUE";

    public static final PropertyDependencies EMPTY_PROPERTY_DEPENDENCIES = new PropertyDependencies(new HashMap<>());
    private static PropertyDependencies defaultPropertyDependencies = null;

    /**
     * Зависимости между разными свойствами.
     */
    @NonNull
    private final Map<Class<?>, Map<Object, List<Link>>> dependencies;

    public PropertyDependencies(Map<Class<?>, Map<Object, List<Link>>> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<Class<?>, Pair<Class<?>, Object>> getReverseDependencies() {
        Map<Class<?>, Pair<Class<?>, Object>> reverseDependencies = new HashMap<>();
        dependencies.entrySet().forEach(entry -> {
            entry.getValue().forEach((o, list) -> {
                list.forEach(link -> {
                    reverseDependencies.put(link.dependentPropertyType, Pair.of(entry.getKey(), o));
                });
            });
        });
        return reverseDependencies;
    }

    public static PropertyDependencies getDefaultPropertyDependencies() {
        if (defaultPropertyDependencies == null) {
            Map<Class<?>, Map<Object, List<Link>>> dependencies = new HashMap<>();

            Map<Object, List<Link>> quantityDependencies = new HashMap<>();
            List<Link> quantityLinks = new ArrayList<>();
            quantityLinks.add(new Link(TransportType.class, DependencyFunction.Max));
            quantityDependencies.put(ANY_VALUE, quantityLinks);
            dependencies.put(QuantityProperty.class, quantityDependencies);

            Map<Object, List<Link>> transportDependencies = new HashMap<>();
            List<Link> railwayLinks = new ArrayList<>();
            railwayLinks.add(new Link(RailwayTrainCarrier.class, DependencyFunction.Max));
            railwayLinks.add(new Link(RailwayTrainNumber.class, DependencyFunction.Max));
            railwayLinks.add(new Link(TrainCarriage.Type.class, DependencyFunction.Sum));
            railwayLinks.add(new Link(TrainBrand.class, DependencyFunction.Max));

            transportDependencies.put(TransportType.TRAIN, railwayLinks);

            List<Link> localTrainLinks = new ArrayList<>();
            localTrainLinks.add(new Link(LocalTrainCarrier.class, DependencyFunction.Max));
            localTrainLinks.add(new Link(LocalTrainNumber.class, DependencyFunction.Max));

            transportDependencies.put(TransportType.LOCAL_TRAIN, localTrainLinks);

            dependencies.put(TransportType.class, transportDependencies);

            Map<Object, List<Link>> railwayTrainCarriageDependencies = new HashMap<>();
            List<Link> coupeSeatLinks = new ArrayList<>();
            coupeSeatLinks.add(new Link(SeatAttribute.Coupe.UpperLower.class, DependencyFunction.Sum));
            coupeSeatLinks.add(new Link(SeatAttribute.Coupe.WcProximity.class, DependencyFunction.Sum));

            railwayTrainCarriageDependencies.put(TrainCarriage.Type.COUPE, coupeSeatLinks);

            List<Link> openPlanLinks = new ArrayList<>();
            openPlanLinks.add(new Link(SeatAttribute.OpenPlan.Side.class, DependencyFunction.Sum));
            openPlanLinks.add(new Link(SeatAttribute.OpenPlan.UpperLower.class, DependencyFunction.Sum));
            openPlanLinks.add(new Link(SeatAttribute.OpenPlan.WcProximity.class, DependencyFunction.Sum));

            railwayTrainCarriageDependencies.put(TrainCarriage.Type.OPEN_PLAN, openPlanLinks);

            List<Link> localSeatLinks = new ArrayList<>();
            localSeatLinks.add(new Link(SeatAttribute.Local.Side.class, DependencyFunction.Sum));

            railwayTrainCarriageDependencies.put(TrainCarriage.Type.LOCAL, localSeatLinks);

            dependencies.put(TrainCarriage.Type.class, railwayTrainCarriageDependencies);

            defaultPropertyDependencies = new PropertyDependencies(dependencies);
        }
        return defaultPropertyDependencies;
    }

    public boolean checkConsistency(Map<Class<?>, List<Object>> properties) {
        for (Map.Entry<Class<?>, Map<Object, List<Link>>> typeValueEntry : dependencies.entrySet()) {
            Class mainType = typeValueEntry.getKey();
            for (Map.Entry<Object, List<Link>> dependencyEntry : typeValueEntry.getValue().entrySet()) {
                Object value = dependencyEntry.getKey();
                for (Link link : dependencyEntry.getValue()) {
                    Class dependentPropertyType = link.dependentPropertyType;
                    if (properties.containsKey(dependentPropertyType)) {
                        if (!properties.containsKey(mainType)) {
                            return false;
                        }
                        if (!value.equals(ANY_VALUE) && !properties.get(mainType).contains(value)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static class Link {
        public Class<?> dependentPropertyType;
        public DependencyFunction dependencyFunction;

        public Link(Class<?> dependentPropertyType, DependencyFunction dependencyFunction) {
            this.dependentPropertyType = dependentPropertyType;
            this.dependencyFunction = dependencyFunction;
        }
    }

    public enum DependencyFunction implements Function<List<Integer>, Integer> {
        Sum {
            @Override
            public Integer apply(List<Integer> integers) {
                int sum = 0;
                for (int i : integers) {
                    sum += i;
                }
                return sum;
            }
        },
        Max {
            @Override
            public Integer apply(List<Integer> integers) {
                if (integers.isEmpty()) {
                    return 0;
                } else {
                    int max = integers.get(0);
                    for (int i : integers) {
                        max = Math.max(max, i);
                    }
                    return max;
                }
            }
        }
    }

    public static class QuantityProperty {

    }

    public static class TransfersProperty {

    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class LocalTrainNumber implements Serializable {
        protected TrainNumber trainNumber;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class RailwayTrainNumber implements Serializable {
        protected TrainNumber trainNumber;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class LocalTrainCarrier implements Serializable {
        public Ref<RailwayCarrier> railwayCarrier;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class RailwayTrainCarrier implements Serializable {
        public Ref<RailwayCarrier> railwayCarrier;
    }

}
