package com.khovanskiy.runtime;

import com.khovanskiy.model.Path;
import com.khovanskiy.model.runtime.RouteBuilderResponse;

import java.util.List;

/**
 * @author victor
 */
public class DefaultRouteBuilderResponse extends RouteBuilderResponse<Path, DefaultRouteBuilderFilter> {

    public DefaultRouteBuilderResponse(List<Path> transportationOfferList, DefaultRouteBuilderFilter filter, long requestId) {
        super(transportationOfferList, filter, requestId);
    }
}