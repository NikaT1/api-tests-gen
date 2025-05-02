package ru.itmo.ivt.apitestgenplugin.model;

import lombok.Builder;
import ru.itmo.ivt.apitestgenplugin.model.enums.AuthType;

@Builder
public record UserInput(String openApiPath, String testConfigPath, String dataConfigPath, AuthType authType,
                        String baseUrl) {
}
