package ru.itmo.ivt.apitestgenplugin.util;

import io.swagger.v3.oas.models.Operation;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class StringUtils {
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String kebabToCamelCase(String str) {
        return Arrays.stream(str.split("[^a-zA-Z0-9]"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce("", String::concat);
    }

    public static String camelToSnakeCase(String str) {
        return IntStream.range(0, str.length())
                .mapToObj(i -> {
                    char currentChar = str.charAt(i);
                    if (i == 0) {
                        return String.valueOf(Character.toLowerCase(currentChar));
                    }

                    char prevChar = str.charAt(i - 1);
                    if (Character.isUpperCase(currentChar)) {
                        boolean shouldAddUnderscore = !Character.isUpperCase(prevChar) ||
                                (i < str.length() - 1 &&
                                        Character.isLowerCase(str.charAt(i + 1)));
                        return (shouldAddUnderscore ? "_" : "") +
                                Character.toLowerCase(currentChar);
                    }
                    return String.valueOf(currentChar);
                })
                .collect(Collectors.joining());
    }

    public static String mapToString(Map<String, String> map) {
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public static String getControllerName(Operation operation) {
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            return capitalize(operation.getTags().get(0));
        }
        return "DefaultController";
    }

    public static String getOperationName(Operation operation, String httpMethod) {
        if (operation.getOperationId() != null) {
            return capitalize(operation.getOperationId().replaceAll("[^a-zA-Z0-9]", ""));
        }
        return capitalize(httpMethod) + "Operation";
    }
}
