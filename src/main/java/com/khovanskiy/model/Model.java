package com.khovanskiy.model;

import lombok.Data;

/**
 * @author victor
 */
@Data
public abstract class Model<M extends Model<M>> {
    protected Ref<M> id;
    protected State state = State.NEW;
}
