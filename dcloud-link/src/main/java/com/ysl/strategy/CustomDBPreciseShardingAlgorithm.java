package com.ysl.strategy;


import com.ysl.enums.BizCodeEnum;
import com.ysl.exception.BizException;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public class CustomDBPreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {


    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String dbPrefix = ShardingDBConfig.getRandomDBPrefix(shardingValue.getValue());

        for (String targetName : availableTargetNames) {
            String targetNameSuffix = targetName.substring(targetName.length() - 1);

            if (dbPrefix.equals(targetNameSuffix)) {
                return targetName;
            }
        }

        throw new BizException(BizCodeEnum.DB_ROUTE_NOT_FOUND);

    }

}
