package com.ysl.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 设置可用单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // 序列化的时候序列对象的所有属性
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        // 反序列化的时候如果多了其他属性,不抛出异常
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 如果是空对象的时候,不抛异常
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static String obj2Json(Object obj) {
        String jsonStr = null;
        try {
            jsonStr = mapper.writeValueAsString(obj);
        } catch (Exception e) {
            // TODO: handle exception
            log.error("json格式化异常:{}", e);
        }
        return jsonStr;
    }

    public static <T> T json2Obj(String jsonStr, Class<T> beanType) {
        T obj = null;
        try {
            obj = mapper.readValue(jsonStr, beanType);

        } catch (Exception e) {
            // TODO: handle exception
            log.error("json格式化异常:{}", e);
        }
        return obj;
    }

    public static <T> List<T> json2List(String jsonData, Class<T> beanType) {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = mapper.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            // e.printStackTrace();
            log.error("json格式化异常:{}", e);
        }
        return null;
    }

    public static byte[] obj2Bytes(Object obj) {
        byte[] byteArr = null;
        try {
            byteArr = mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            // e.printStackTrace();
            log.error("json格式化异常:{}", e);
        }
        return byteArr;
    }

    public static <T> T bytes2Obj(byte[] byteArr, Class<T> beanType) {
        T obj = null;
        try {
            obj = mapper.readValue(byteArr, beanType);
        } catch (Exception e) {
            // e.printStackTrace();
            log.error("json格式化异常:{}", e);
        }
        return obj;
    }
}
