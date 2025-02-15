package com.github.herdeny.healthaiv2.utils;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class JsonTools {

    public void saveJsonToFile(JSONObject jsonObject, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(jsonObject.toString()); // 格式化输出
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
