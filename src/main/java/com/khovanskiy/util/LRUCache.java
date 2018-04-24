package com.khovanskiy.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author victor
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private int maxCapacity;

    public LRUCache(int maxCapacity) {
        super(maxCapacity + 1, 1.0f, true);
        this.maxCapacity = maxCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= maxCapacity;
    }
}
