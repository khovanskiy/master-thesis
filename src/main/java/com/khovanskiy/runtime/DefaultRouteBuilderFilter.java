package com.khovanskiy.runtime;

import com.khovanskiy.model.Path;
import com.khovanskiy.model.runtime.RouteBuilderFilter;

/**
 * @author victor
 */
public class DefaultRouteBuilderFilter implements RouteBuilderFilter<Path> {

    @Override
    public int getMaxTransfersCount() {
        return 3;
    }

    @Override
    public int getRequiredQuantity() {
        return 0;
    }

    @Override
    public boolean test(Path path) {
        return true;
    }
}
