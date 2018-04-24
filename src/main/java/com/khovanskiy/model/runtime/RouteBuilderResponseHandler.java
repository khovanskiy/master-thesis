package com.khovanskiy.model.runtime;

import com.khovanskiy.model.Path;
import com.khovanskiy.model.Properties;

import java.util.Iterator;
import java.util.List;

/**
 * @author victor
 */
public interface RouteBuilderResponseHandler<R extends RouteBuilderResponse<S, F>, S, F extends RouteBuilderFilter<S>> {

    S handlePath(Path path);

    boolean isValidPath(S s);

    boolean isEmpty(F f);

    F handleProperties(Properties properties, int maxTransfersCount, int requiredQuantity);

    Properties.PropertiesFilter handleFilter(F f);

    R handleResponse(List<S> paths, F f, long requestId);

    default R defaultResponse(List<S> paths, Properties properties, long requestId) {
        return handleResponse(paths, handleProperties(properties, 0, 0), requestId);
    }

    default Iterator<S> getHandlingIterator(Iterator<Path> baseIterator) {
        return new Iterator<S>() {
            @Override
            public boolean hasNext() {
                return baseIterator.hasNext();
            }

            @Override
            public S next() {
                Path path = baseIterator.next();
                assert path != null;
                return handlePath(path);
            }
        };
    }
}
