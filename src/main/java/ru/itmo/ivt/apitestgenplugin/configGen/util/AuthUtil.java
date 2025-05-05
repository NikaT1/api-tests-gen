package ru.itmo.ivt.apitestgenplugin.configGen.util;

import lombok.experimental.UtilityClass;
import ru.itmo.ivt.apitestgenplugin.model.enums.AuthType;

import java.util.Properties;

@UtilityClass
public class AuthUtil {
    private static final String DEFAULT_PROPERTY_VALUE = "true";

    public static void setAuthProperty(AuthType authType, Properties properties) {
        switch (authType) {
            case NO -> properties.setProperty("NO_AUTH", DEFAULT_PROPERTY_VALUE);
            case TOKEN -> properties.setProperty("TOKEN_AUTH", DEFAULT_PROPERTY_VALUE);
            case BASIC -> properties.setProperty("BASIC_AUTH", DEFAULT_PROPERTY_VALUE);
        }
    }
}
