package com.zyfan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Arrays;

@Configuration
public class FeignConfig {
    @Bean
    public Decoder feignDecoder() {
        // 1. 创建ObjectMapper，可在此自定义序列化规则
        // 例如：objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. 创建消息转换器，并支持更多媒体类型
        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        // 添加更多常见的JSON相关媒体类型，增强兼容性
        jacksonConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.valueOf("text/json"), // 处理不标准的"text/json"
                MediaType.valueOf("application/json;charset=UTF-8") // 处理带字符集的类型
        ));

        // 3. 使用SpringDecoder和ResponseEntityDecoder进行包装
        ObjectFactory<HttpMessageConverters> objectFactory =
                () -> new HttpMessageConverters(jacksonConverter);
        SpringDecoder springDecoder = new SpringDecoder(objectFactory);

        // ResponseEntityDecoder提供了更好的响应处理（如处理404等状态码）
        return new ResponseEntityDecoder(springDecoder);
    }
}