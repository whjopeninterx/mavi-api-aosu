package com.openinterx.mavi.util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openinterx.mavi.exception.XvuException;

import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // String -> JsonNode（等价于 JSONObject）
    public static JsonNode strToJSONObj(String jsonStr) {
        try {
            return objectMapper.readTree(jsonStr);
        } catch (Exception e) {
            throw new XvuException("Invalid JSON string", e);
        }
    }

    // String -> JsonNode（等价于 JSONArray）
    public static JsonNode strToJSONArr(String jsonStr) {
        try {
            return objectMapper.readTree(jsonStr);
        } catch (Exception e) {
            throw new XvuException("Invalid JSON array string", e);
        }
    }

    // String -> Java Object
    public static <T> T strToObj(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            throw new XvuException("Failed to deserialize JSON", e);
        }
    }

    // String -> List<T>
    public static <T> List<T> strToList(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new XvuException("Failed to deserialize JSON to List", e);
        }
    }

    // Object -> JSON String
    public static String objToStr(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new XvuException("Failed to serialize object", e);
        }
    }

    // Object -> JsonNode（类似 JSONObject）
    public static JsonNode objToJSONObj(Object obj) {
        return objectMapper.valueToTree(obj);
    }

    // JsonNode -> Object
    public static <T> T JSONObjToObj(JsonNode node, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            throw new XvuException("Failed to convert JSON node to object", e);
        }
    }

    // JsonNode -> String
    public static String JSONObjToStr(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new XvuException("Failed to convert JSON node to string", e);
        }
    }

    // JsonNode (Array) -> List<T>
    public static <T> List<T> JSONArrToList(JsonNode node, Class<T> clazz) {
        try {
            return objectMapper.readValue(node.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new XvuException("Failed to convert JSON array to List", e);
        }
    }

    // List -> JsonNode (Array)
    public static JsonNode listToJSONArr(List<?> list) {
        return objectMapper.valueToTree(list);
    }

    // JSON String -> Map
    public static Map<String, Object> jsonToMap(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            throw new XvuException("JSON string cannot be null or empty");
        }
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<>() {});
        } catch (Exception e) {
            throw new XvuException("Failed to parse JSON string to Map", e);
        }
    }

    // Map -> JSON String
    public static String mapToJsonString(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new XvuException("Failed to convert Map to JSON string", e);
        }
    }

    // 判断是否是合法 JSON
    public static boolean isJSON(String str) {
        try {
            objectMapper.readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
