package com.coderzoe.constants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 目前大部分项目中都存在两套json转化工具：jackson和fastjson 这导致很多POJO类上会同时标注jackson和fastjson两套注解
 * 考虑到spring web mvc默认使用的是jackson且当前fastjson的口碑，目前主流都是建议jackson，但jackson缺乏简单的api，因此这里做个工具类封装一下
 * @author yinhuasheng
 * @email yinhuasheng@unicloud.com
 * @date 2023/7/26 16:44
 */
public class JsonUtil {
    /**
     * {@link ObjectMapper}本身是线程安全的，因此全局唯一就够了，初始化的时候建好
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toJsonString(Object target){
        try {
            return OBJECT_MAPPER.writeValueAsString(target);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static<T> T parseObject(String json,Class<T> tClass){
        try {
            return OBJECT_MAPPER.readValue(json,tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseObject(String json){
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
