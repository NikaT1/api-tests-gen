package ru.itmo.ivt.apitestgenplugin.modelGen;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import ru.itmo.ivt.apitestgenplugin.model.userconfig.ModelConfiguration;
import ru.itmo.ivt.apitestgenplugin.model.userconfig.TestConfiguration;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class ConfigParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static TestConfiguration parseTestConfig(File configFile) {
        try {
            return objectMapper.readValue(configFile, TestConfiguration.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static ModelConfiguration parseModelConfig(File configFile) {
        try {
            return objectMapper.readValue(configFile, ModelConfiguration.class);
        } catch (Exception e) {
            return null;
        }
    }
}