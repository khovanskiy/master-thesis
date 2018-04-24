package com.khovanskiy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Прямой сегмент
 *
 * @author victor
 */
@AllArgsConstructor
public class ForwardSegment implements PathSegment {
    private final Stop A;
    private final Stop B;
    private final Stop C;
    @Getter
    private final Properties properties;

    /**
     * @return точку A
     */
    public Stop A() {
        return A;
    }

    /**
     * @return точку B
     */
    public Stop B() {
        return B;
    }

    /**
     * @return точку C
     */
    public Stop C() {
        return C;
    }

    public boolean isTransfer() {
        return !A.getRef().equals(C.getRef());
    }

    public String toString() {
        return "Segment(A=" + A + ", B=" + B + ", C=" + C + ")";
    }

    @Override
    public Stop start() {
        return A;
    }

    @Override
    public Stop end() {
        return B;
    }
}
