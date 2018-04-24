package com.khovanskiy.model;

import com.khovanskiy.util.Reflections;

import java.io.Serializable;

/**
 * @author victor
 */
public interface Pointer<M> extends Serializable {
    default Class<M> type() {
        return Reflections.genericClassTypeOf(this);
    }

    @SuppressWarnings("unchecked")
    default <X extends Pointer> X cast() {
        return (X) this;
    }
}
