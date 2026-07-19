package com.ysl.util;

import org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator;

public class IDUtil {


    private static final SnowflakeShardingKeyGenerator shardingKeyGenerator = new SnowflakeShardingKeyGenerator();


    public static Comparable<?> geneSnowFlakeID() {

        return shardingKeyGenerator.generateKey();
    }

}