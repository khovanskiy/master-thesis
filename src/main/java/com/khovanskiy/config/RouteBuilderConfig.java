package com.khovanskiy.config;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
public class RouteBuilderConfig {
    private int maxTransfersCount;
    private int minTransferTime;
    private int maxTransferTime;
    private int maxCacheSize;
    private int maxNumberOfResult;
}
