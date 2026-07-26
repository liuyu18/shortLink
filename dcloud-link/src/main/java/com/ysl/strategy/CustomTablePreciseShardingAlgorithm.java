package com.ysl.strategy;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public class CustomTablePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String targetName = availableTargetNames.iterator().next();
        String value = shardingValue.getValue();
        String codeSuffix = value.substring(value.length() - 1);

        return targetName + "_" + codeSuffix;
    }
}
