package com.ysl.strategy;

import java.util.ArrayList;
import java.util.List;

public class ShardingTableConfig {
    private static final List<String> tableSuffixList = new ArrayList<>();

    static {
        tableSuffixList.add("0");
        tableSuffixList.add("a");
    }

    public static String getRandomSuffix(String code) {
        int hashCode = code.hashCode();
        int index = Math.abs(hashCode) % tableSuffixList.size();
        return tableSuffixList.get(index);
    }
}