package com.ysl.strategy;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public class CustomTablePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String tableSuffix = ShardingTableConfig.getRandomSuffix(shardingValue.getValue());

        for (String targetName : availableTargetNames) {
            if (targetName.endsWith("_" + tableSuffix)) {
                return targetName;
            }
        }

        throw new IllegalArgumentException("No short_link table found for suffix: " + tableSuffix);
    }
}
