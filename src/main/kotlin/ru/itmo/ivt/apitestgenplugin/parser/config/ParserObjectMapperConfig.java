package ru.itmo.ivt.apitestgenplugin.parser.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ParserObjectMapperConfig {
    private final static ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

    public static ObjectMapper getObjectMapper() {
        return DEFAULT_OBJECT_MAPPER;
    }
}
