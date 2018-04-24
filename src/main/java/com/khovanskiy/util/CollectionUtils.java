package com.khovanskiy.util;

import java.util.Comparator;
import java.util.List;

/**
 * @author victor
 */
public class CollectionUtils {
    public static <T> int lowerBound(List<? extends T> l, T key, Comparator<? super T> c) {
        int len = l.size();
        if (len == 0) {
            return 0;
        }
        int low = 0;
        int high = len - 1;
        int mid = (low + high) >>> 1;
        while (true) {
            T midVal = l.get(mid);
            int cmp = c.compare(midVal, key);
            if (cmp == 0 || cmp > 0) {
                high = mid - 1;
                if (high < low)
                    return mid;
            } else {
                low = mid + 1;
                if (high < low)
                    return mid < len - 1 ? mid + 1 : len;
            }
            mid = (low + high) >>> 1;
        }
    }

    public static <T> int upperBound(List<? extends T> l, T key, Comparator<? super T> c) {
        int len = l.size();
        if (len == 0) {
            return 0;
        }
        int low = 0;
        int high = len - 1;
        int mid = (low + high) >>> 1;
        while (true) {
            T midVal = l.get(mid);
            int cmp = c.compare(midVal, key);
            if (cmp == 0 || cmp < 0) {
                low = mid + 1;
                if (high < low)
                    return mid < len - 1 ? mid + 1 : len;
            } else {
                high = mid - 1;
                if (high < low)
                    return mid;
            }
            mid = (low + high) >>> 1;
        }
    }

}
