package com.example.appearanceRater.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
