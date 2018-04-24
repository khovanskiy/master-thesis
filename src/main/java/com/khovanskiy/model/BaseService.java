package com.khovanskiy.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author victor
 */
@Getter
@Setter
public abstract class BaseService<B extends BaseService<B>> extends Model<B> {
    /**
     * Название услуги
     */
    protected String name;
}
