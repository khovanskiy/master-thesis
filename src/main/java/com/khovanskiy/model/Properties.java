package com.khovanskiy.model;

import com.khovanskiy.model.runtime.FilteringMode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * victor
 */
@EqualsAndHashCode
@ToString(callSuper = true)
public class Properties implements Serializable {
    private final Map<Class<?>, Map<Object, Integer>> data;
    private final static Properties EMPTY = new Properties();

    public static Properties empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public Properties() {
        this.data = Collections.emptyMap();
    }

    public Properties(Map<Class<?>, Map<Object, Integer>> properties) {
        this.data = properties;
    }

    public Properties min(Properties other) {
        return min(this, other);
    }

    public static Properties min(Properties a, Properties b) {
        if (a.isEmpty()) {
            return b;
        }
        if (b.isEmpty()) {
            return a;
        }
        Map<Class<?>, Map<Object, Integer>> properties = new HashMap<>();
        Stream.concat(a.data.keySet().stream(), b.data.keySet().stream()).distinct().forEach(type -> {
            Map<Object, Integer> map = new HashMap<>();
            Map<Object, Integer> amap = a.data.getOrDefault(type, Collections.emptyMap());
            Map<Object, Integer> bmap = b.data.getOrDefault(type, Collections.emptyMap());
            if (amap.isEmpty()) {
                map.putAll(bmap);
            } else if (bmap.isEmpty()) {
                map.putAll(amap);
            } else {
                amap.forEach((value, aCount) -> {
                    Integer bCount = bmap.get(value);
                    if (bCount != null) {
                        map.put(value, Math.min(aCount, bCount));
                    }
                });
            }
            properties.put(type, map);
        });
        return new Properties(properties);
    }

    public Properties max(Properties other) {
        return max(this, other);
    }

    public static Properties max(Properties a, Properties b) {
        if (a.isEmpty()) {
            return b;
        }
        if (b.isEmpty()) {
            return a;
        }
        Map<Class<?>, Map<Object, Integer>> properties = new HashMap<>();
        Stream.concat(a.data.keySet().stream(), b.data.keySet().stream()).distinct().forEach(type -> {
            Map<Object, Integer> map = new HashMap<>();
            Map<Object, Integer> amap = a.data.getOrDefault(type, Collections.emptyMap());
            map.putAll(amap);
            Map<Object, Integer> bmap = b.data.getOrDefault(type, Collections.emptyMap());
            bmap.forEach((value, count) -> map.merge(value, count, Math::max));
            properties.put(type, map);
        });
        return new Properties(properties);
    }

    public PropertiesTree asPropertiesTree() {
        return new PropertiesTree(this, PropertyDependencies.getDefaultPropertyDependencies());
    }



    @AllArgsConstructor
    @Getter
    @ToString(callSuper = true)
    public static class PropertiesFilter implements Predicate<Properties> {
        private static final int DEFAULT_REQUIRED_QUANTITY = 1;
        private static final int DEFAULT_MAX_TRANSFERS = 3;

        protected final int requiredQuantity;

        protected final int maxTransfers;

        protected Map<Class<?>, List<Object>> markedProperties;

        protected final FilteringMode filteringMode;

        public static PropertiesFilter empty() {
            return new PropertiesFilter(DEFAULT_REQUIRED_QUANTITY, DEFAULT_MAX_TRANSFERS, new HashMap<>(), FilteringMode.BlackList);
        }

        public void addMarkedValue(Class<?> markedType, Object markedValue) {
            List<Object> markedValues = this.markedProperties.getOrDefault(markedType, new ArrayList<>());
            markedValues.add(markedValue);
            this.markedProperties.put(markedType, markedValues);
        }

        public int getMaxQuantity(Properties properties) {
            return getMaxQuantity(properties, PropertyDependencies.getDefaultPropertyDependencies());
        }

        public int getMaxQuantity(Properties properties, PropertyDependencies propertyDependencies) {
            return getWithRestrictions(properties, propertyDependencies,
                    PropertyDependencies.QuantityProperty.class, PropertyDependencies.ANY_VALUE);
        }

        public boolean test(Properties properties) {
            return test(properties, PropertyDependencies.getDefaultPropertyDependencies());
        }

        public boolean test(Properties properties, PropertyDependencies propertyDependencies) {
            return getMaxQuantity(properties, propertyDependencies) >= requiredQuantity;
        }

        protected int getWithRestrictions(Properties properties, PropertyDependencies propertyDependencies,
                                          Class<?> propertyType, Object propertyValue) {
            Map<Object, Integer> typeMap = properties.data.getOrDefault(propertyType, Collections.emptyMap());
            int value = typeMap.getOrDefault(propertyValue, 0);
            Map<Object, List<PropertyDependencies.Link>> typeDependencies = propertyDependencies.getDependencies()
                    .getOrDefault(propertyType, Collections.emptyMap());
            if (!typeDependencies.isEmpty()) {
                List<PropertyDependencies.Link> dependencyLinks;
                if (typeDependencies.containsKey(PropertyDependencies.ANY_VALUE)) {
                    dependencyLinks = typeDependencies.get(PropertyDependencies.ANY_VALUE);
                } else {
                    dependencyLinks = typeDependencies.getOrDefault(propertyValue, Collections.emptyList());
                }
                for (PropertyDependencies.Link link : dependencyLinks) {
                    Set<Object> markedValues = new HashSet<>(markedProperties.getOrDefault(link.dependentPropertyType, Collections.emptyList()));
                    Set<Object> existingValues = properties.data.getOrDefault(link.dependentPropertyType, Collections.emptyMap()).keySet();
                    List<Integer> results = new ArrayList<>();
                    for (Object existingValue : existingValues) {
                        switch (filteringMode) {
                            case BlackList: {
                                if (!markedValues.contains(existingValue)) {
                                    results.add(getWithRestrictions(properties, propertyDependencies,
                                            link.dependentPropertyType, existingValue));
                                }
                            } break;
                            case WhiteList: {
                                if (markedValues.isEmpty() || markedValues.contains(existingValue)) {
                                    results.add(getWithRestrictions(properties, propertyDependencies,
                                            link.dependentPropertyType, existingValue));
                                }
                            } break;
                        }
                    }
                    value = Math.min(value, link.dependencyFunction.apply(results));
                }
            }
            return value;
        }
    }

    public static class PropertiesTree {
        protected Class<?> propertyType;
        protected Object propertyValue;
        protected int quantity;

        protected Map<Class<?>, List<PropertiesTree>> children;

        public PropertiesTree(Properties properties, PropertyDependencies propertyDependencies) {
            this(PropertyDependencies.QuantityProperty.class, PropertyDependencies.ANY_VALUE,
                    properties, propertyDependencies);
        }

        protected PropertiesTree(Class<?> propertyType, Object propertyValue,
                                 Properties properties, PropertyDependencies propertyDependencies) {
            this.propertyType = propertyType;
            this.propertyValue = propertyValue;
            Map<Object, Integer> typeMap = properties.data.getOrDefault(propertyType, Collections.emptyMap());
            this.quantity = typeMap.getOrDefault(propertyValue, 0);
            this.children = new HashMap<>();
            Map<Object, List<PropertyDependencies.Link>> typeDependencies = propertyDependencies.getDependencies()
                    .getOrDefault(propertyType, Collections.emptyMap());
            if (!typeDependencies.isEmpty()) {
                List<PropertyDependencies.Link> dependencyLinks;
                if (typeDependencies.containsKey(PropertyDependencies.ANY_VALUE)) {
                    dependencyLinks = typeDependencies.get(PropertyDependencies.ANY_VALUE);
                } else {
                    dependencyLinks = typeDependencies.getOrDefault(propertyValue, Collections.emptyList());
                }
                for (PropertyDependencies.Link link : dependencyLinks) {
                    Set<Object> existingValues = properties.data.getOrDefault(link.dependentPropertyType,
                            Collections.emptyMap()).keySet();
                    List<PropertiesTree> typedChildren = new ArrayList<>();
                    for (Object existingValue : existingValues) {
                        typedChildren.add(new PropertiesTree(link.dependentPropertyType, existingValue,
                                properties, propertyDependencies));
                    }
                    children.put(link.dependentPropertyType, typedChildren);
                }
            }
        }

        public void prune(int requiredQuantity) {
            Set<Class<?>> dependentTypes = new HashSet<>(children.keySet());
            for (Class<?> dependentType : dependentTypes) {
                List<PropertiesTree> newChildren = new ArrayList<>();
                for (PropertiesTree child : children.get(dependentType)) {
                    if (child.quantity >= requiredQuantity) {
                        child.prune(requiredQuantity);
                        newChildren.add(child);
                    }
                }
                //if (dependentType == TransportType.class || newChildren.size() > 1) {
                if (newChildren.size() > 0) {
                    newChildren = newChildren.stream().filter(p -> p.propertyValue != null).collect(Collectors.toList());
                    children.put(dependentType, newChildren);
                } else {
                    children.remove(dependentType);
                }
            }
        }

        public Map<Class<?>, List<Object>> getPropertyValues() {
            Map<Class<?>, List<Object>> propertyValues = new HashMap<>();
            for (Class<?> dependentType : children.keySet()) {
                List<Object> values = new ArrayList<>();
                for (PropertiesTree child : children.get(dependentType)) {
                    propertyValues.putAll(child.getPropertyValues());
                    values.add(child.propertyValue);
                }
                propertyValues.put(dependentType, values);
            }
            return propertyValues;
        }
    }
}
