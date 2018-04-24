package com.khovanskiy.util;

/**
 * @author victor
 */
public class IdxGen<T> {
    private int nextIdx = 0;

    @Deprecated
    public Idx<T> get() {
        return new Idx<>(nextIdx++);
    }

    public int getNextIdxValue() {
        return nextIdx;
    }
}
