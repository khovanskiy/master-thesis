package com.khovanskiy.service;

import com.khovanskiy.model.Model;
import com.khovanskiy.model.Ref;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Заглушка для базы данных
 *
 * @author victor
 */
public class Repository {

    private Map<Class, Map<Ref, Object>> objects = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <M extends Model<M>> Optional<M> find(@NonNull Ref<M> id) {
        M model = (M) objects.getOrDefault(id.type(), Collections.emptyMap()).get(id);
        if (model == null) {
            return Optional.empty();
        }
        return Optional.of(model);
    }

    public <M extends Model<M>> M create(@NonNull M model) {
        objects.compute(model.getId().type(), (key, map) -> {
            if (map == null) {
                map = new HashMap<>();
            }
            map.put(model.getId(), model);
            return map;
        });
        return model;
    }

    @SuppressWarnings("unchecked")
    public <M extends Model<M>> List<M> findAll(Class<M> type) {
        return new ArrayList(objects.getOrDefault(type, Collections.emptyMap()).values());
    }


    public void clear() {
        objects.clear();
    }
}
