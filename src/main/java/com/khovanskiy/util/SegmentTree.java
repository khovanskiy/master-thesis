package com.khovanskiy.util;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Класс дерева отрезков, позволящий выполнять любую ассоциативную операцию
 * на подотрезке.
 *
 * @author victor
 */
public class SegmentTree<E> implements Serializable {

    private static final long serialVersionUID = 8683452521421592189L;

    /**
     * Ассоциативная операция над элементами.
     */
    protected SerializableBiFunction<E> function;

    /**
     * Нейтральный элемент операции.
     */
    protected E neutral;

    /**
     * Узлы дерева
     */

    protected ArrayList<E> segments;

    @Getter
    protected ArrayList<E> collection;

    // Used only for serialization
    /*protected SegmentTree() {
        this.neutral = null;
        this.function = null;
        this.segments = null;
        this.collection = null;
    }/**/

    public SegmentTree(Collection<? extends E> collection, SerializableBiFunction<E> function, E neutral) {
        int size = collection.size();
        segments = new ArrayList<>(size * 2);
        for (int i = 0; i < size; i++) {
            segments.add(neutral);
        }
        segments.addAll(collection);
        for (int i = size - 1; i > 0; --i) {
            segments.set(i, function.apply(segments.get(2 * i), segments.get(2 * i + 1)));
        }
        this.function = function;
        this.neutral = neutral;
        this.collection = new ArrayList<>(collection);
    }

    /**
     * Конструктор дерева отрезков для заглушек.
     * Всегда возвращает нейтральный элемент при запросах select.
     * @param neutral нейтральный элемент
     */
    public SegmentTree(E neutral) {
        function = new ConstantSerializableBiFunction<>(neutral);
        segments = new ArrayList<>();
        this.neutral = neutral;
        collection = new ArrayList<>();
    }

    /**
     * Возвращает результат ассоциативной операции на подотрезке.
     * Левая граница подотрезка должна быть не больше правой.
     * В случае нулевого пересечения запроса с отрезком возвращается нейтральный элемент.
     * @param l левая граница подотрезка (включительно)
     * @param r правая граница подотрезка (не включительно)
     * @return результат ассоциативной операции над элементами с индексами из подотрезка
     * [0, size) intersect [l, r)
     */
    public E select(int l, int r) {
        assert l <= r;
        int size = collection.size();
        if (r <= 0 || l >= size) {
            return neutral;
        }/**/
        l = Math.max(0, l);
        r = Math.min(r, size);/**/
        E result = neutral;
        for (l += size, r += size; l < r; l >>= 1, r >>= 1) {
            if (l % 2 == 1) {
                result = function.apply(result, segments.get(l++));
            }
            if (r % 2 == 1) {
                result = function.apply(result, segments.get(--r));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "SegmentTree {" +
                " neutral = " + neutral.toString() +
                ", segments = " + Arrays.toString(segments.toArray()) +
                " }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SegmentTree<?> that = (SegmentTree<?>) o;

        return (segments.equals(that.segments));

    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }/**/

    public interface SerializableBiFunction<T> extends BiFunction<T, T, T>, Serializable {
    }

    public class ConstantSerializableBiFunction<T> implements SerializableBiFunction<T> {
        private T t;

        public ConstantSerializableBiFunction(T t) {
            this.t = t;
        }

        @Override
        public T apply(T t, T t2) {
            return t;
        }
    }
}
