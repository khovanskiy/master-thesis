package com.khovanskiy.runtime;

import com.khovanskiy.model.Path;
import com.khovanskiy.model.Properties;
import com.khovanskiy.model.PropertyDependencies;
import com.khovanskiy.model.runtime.FilteringMode;
import com.khovanskiy.model.runtime.RouteBuilderResponseHandler;

import java.util.Collections;
import java.util.List;

/**
 * @author victor
 */
public class DefaultRouteBuilderHandler implements RouteBuilderResponseHandler<DefaultRouteBuilderResponse, Path, DefaultRouteBuilderFilter> {

    @Override
    public Path handlePath(Path path) {
        return path;
    }

    @Override
    public boolean isValidPath(Path path) {
        return true;
    }

    @Override
    public boolean isEmpty(DefaultRouteBuilderFilter defaultRouteBuilderFilter) {
        return true;
    }

    @Override
    public DefaultRouteBuilderFilter handleProperties(Properties properties, int maxTransfersCount, int requiredQuantity) {
        return null;
    }

    @Override
    public Properties.PropertiesFilter handleFilter(DefaultRouteBuilderFilter defaultRouteBuilderFilter) {
        return new Properties.PropertiesFilter(1, 3, Collections.emptyMap(), FilteringMode.BlackList) {
            @Override
            public boolean test(Properties properties) {
                return true;
            }

            @Override
            public boolean test(Properties properties, PropertyDependencies propertyDependencies) {
                return true;
            }
        };
    }

    @Override
    public DefaultRouteBuilderResponse handleResponse(List<Path> paths, DefaultRouteBuilderFilter defaultRouteBuilderFilter, long requestId) {
        return new DefaultRouteBuilderResponse(paths, defaultRouteBuilderFilter, requestId);
    }
}
