package com.ysl.strategy;

import java.util.ArrayList;
import java.util.List;

public class ShardingDBConfig {
    private static final List<String> dbPrefixList = new ArrayList<>();
    static {
        dbPrefixList.add("0");
        dbPrefixList.add("1");
        dbPrefixList.add("a");
    }

    public static String getRandomDBPrefix(String code) {
        int hashCode = code.hashCode();
        int index = Math.abs(hashCode) % dbPrefixList.size();
        return dbPrefixList.get(index);
    }
}
