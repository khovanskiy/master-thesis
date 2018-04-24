package com.khovanskiy.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author victor
 */
public class Timeline<V> implements Iterable<Timeline.Entry<V>> {
    private static final Comparator<? super Entry> COMPARATOR = (lhs, rhs) -> lhs.getKey().compareTo(rhs.getKey());
    private transient Timeline<V> instance;
    private transient Instant fromKey;
    private transient boolean fromInclusive;
    private transient Instant toKey;
    private transient boolean toInclusive;
    private final List<Entry<V>> data = new ArrayList<>();

    protected Timeline(Timeline<V> instance, Instant fromKey, boolean fromInclusive, Instant toKey,
                        boolean toInclusive) {
        this.instance = instance;
        this.fromKey = fromKey;
        this.fromInclusive = fromInclusive;
        this.toKey = toKey;
        this.toInclusive = toInclusive;
    }

    public Timeline() {
        this.instance = this;
    }

    public void put(Instant key, V value) {
        if (key == null) {
            throw new NullPointerException();
        }
        Entry<V> entry = new Entry<>(key, value);
        int index = Collections.binarySearch(data, entry, COMPARATOR);
        if (index < 0) {
            index = -(index + 1);
        }
        instance.data.add(index, entry);
    }

    public Timeline<V> subMap(Instant fromKey, boolean fromInclusive, Instant toKey,
                               boolean toInclusive) {
        return new Timeline<>(this, fromKey, fromInclusive, toKey, toInclusive);
    }

    public Timeline<V> headMap(Instant toKey, boolean inclusive) {
        return new Timeline<>(this, null, false, toKey, inclusive);
    }

    public Timeline<V> tailMap(Instant fromKey, boolean inclusive) {
        return new Timeline<>(this, fromKey, inclusive, null, false);
    }

    @Override
    public Iterator<Entry<V>> iterator() {
        return new TimelineIterator();
    }

    public String toString() {
        return "Timeline(" + this.data + ")";
    }

    public static class Entry<V> implements Map.Entry<Instant, V> {

        private final Instant key;
        private V value;

        public Entry(Instant key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Instant getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    private class TimelineIterator implements Iterator<Entry<V>> {
        private int fromIndex;
        private int toIndex;

        public TimelineIterator() {
            if (fromKey != null) {
                Entry<V> fromEntry = new Entry<>(fromKey, null);
                if (fromInclusive) {
                    fromIndex = CollectionUtils.lowerBound(instance.data, fromEntry, COMPARATOR);
                } else {
                    fromIndex = CollectionUtils.upperBound(instance.data, fromEntry, COMPARATOR);
                }
            }
            if (toKey != null) {
                Entry<V> toEntry = new Entry<>(toKey, null);
                if (toInclusive) {
                    toIndex = CollectionUtils.upperBound(instance.data, toEntry, COMPARATOR);
                } else {
                    toIndex = CollectionUtils.lowerBound(instance.data, toEntry, COMPARATOR);
                }
            } else {
                toIndex = instance.data.size();
            }
        }

        @Override
        public boolean hasNext() {
            return fromIndex < toIndex;
        }

        @Override
        public Entry<V> next() {
            return instance.data.get(fromIndex++);
        }
    }
}
