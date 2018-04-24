package com.khovanskiy.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author victor
 */
public class Reflections {
    @SuppressWarnings("unchecked")
    public static <T> Class<T> genericClassTypeOf(Object o) {
        Class<?> c = o.getClass();
        Class<T> tClass;
        if (c.getGenericSuperclass() instanceof ParameterizedType) {
            tClass = (Class<T>) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0];
        } else {
            Type type = ((ParameterizedType) c.getGenericInterfaces()[0]).getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
                tClass = (Class<T>) ((ParameterizedType) type).getRawType();
            } else {
                tClass = (Class<T>) type;
            }
        }
        return tClass;
    }
}
