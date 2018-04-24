package com.khovanskiy.util;

import com.khovanskiy.model.Pointer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author rurik
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Idx<T> implements Pointer<T> {
    int number;

    public Idx<T> next() {
        return new Idx<>(number + 1);
    }
}
