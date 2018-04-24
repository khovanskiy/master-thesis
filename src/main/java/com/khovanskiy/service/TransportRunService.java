package com.khovanskiy.service;

import com.khovanskiy.model.TransportRun;

/**
 * @author victor
 */
public class TransportRunService {
    //@Override
    public void updateProperties(TransportRun currentModel) {
        //PropertiesFactory propertiesFactory = getPropertiesFactory(currentModel.getTransportType());
        //PropertiesSegmentTree properties = propertiesFactory.createProperties(currentModel.getId());

        /*repository.updateEntity(currentModel.getId(), new Function<TransportRunEntity, TransportRunEntity>() {
            @Override
            public TransportRunEntity apply(TransportRunEntity e) {
                return e.withProperties(properties);
            }
        });/**/
    }
}
