package com.github.herdeny.healthaiv2.utils;

import java.util.*;
import java.util.regex.*;

public class PredictionParser {
    public Map<String, Integer> PredictionParser(String input) {
        // 正则匹配 "2: 203" 的形式
        Pattern pattern = Pattern.compile("(\\d+):\\s*(\\d+)");
        Matcher matcher = pattern.matcher(input);

        // 使用 Map 构造 JSON 数据
        Map<String, Integer> predictionMap = new LinkedHashMap<>();
        while (matcher.find()) {
            String label = matcher.group(1);
            int count = Integer.parseInt(matcher.group(2));
            predictionMap.put(label, count);
        }

        return predictionMap;
    }
}
