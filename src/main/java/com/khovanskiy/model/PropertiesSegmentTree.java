package com.khovanskiy.model;

import com.khovanskiy.util.SegmentTree;

import java.util.Collection;

/**
 * victor
 */
public class PropertiesSegmentTree extends SegmentTree<Properties> {
    public enum PropertiesBiFunction implements SerializableBiFunction<Properties> {
        Min {
            @Override
            public Properties apply(Properties properties, Properties properties2) {
                return Properties.min(properties, properties2);
            }
        }
    }

    public PropertiesSegmentTree(Properties neutral) {
        super(neutral);
    }

    public PropertiesSegmentTree(Collection<? extends Properties> collection,
                                 PropertiesBiFunction function, Properties neutral) {
        super(collection, function, neutral);
    }
}
