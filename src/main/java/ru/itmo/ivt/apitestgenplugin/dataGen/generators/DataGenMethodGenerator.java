package ru.itmo.ivt.apitestgenplugin.dataGen.generators;

import io.swagger.v3.oas.models.media.Schema;
import ru.itmo.ivt.apitestgenplugin.model.DataGenMethod;

import java.util.ArrayList;
import java.util.List;

import static ru.itmo.ivt.apitestgenplugin.util.SchemaUtils.*;
import static ru.itmo.ivt.apitestgenplugin.util.StringUtils.capitalize;
import static ru.itmo.ivt.apitestgenplugin.util.StringUtils.snakeToCamelCase;

public record DataGenMethodGenerator(String modelName, Schema<?> schema) {
    public List<String> getDataGenImports() {
        return List.of("models." + modelName);
    }

    public List<DataGenMethod> generateMethods() {
        List<DataGenMethod> methods = new ArrayList<>();
        methods.addAll(generateCorrectMethod());
        methods.addAll(generateOnlyRequiredMethod());
        methods.addAll(generateIncorrectMethods());
        return methods;
    }

    private List<DataGenMethod> generateCorrectMethod() {
        StringBuilder fields = new StringBuilder();
        schema.getProperties().forEach((name, prop) -> {
            String value = getFieldValue(prop, true);
            fields.append(capitalize(snakeToCamelCase(name))).append("-").append(value).append(" ");
        });
        return List.of(new DataGenMethod("generateCorrect" + modelName, fields.toString().trim()));
    }

    private List<DataGenMethod> generateOnlyRequiredMethod() {
        if (schema.getRequired() == null || schema.getRequired().isEmpty()) {
            return List.of();
        }
        StringBuilder fields = new StringBuilder();
        schema.getRequired().forEach(name -> {
            Schema<?> prop = (Schema<?>) schema.getProperties().get(name);
            String value = getFieldValue(prop, true);
            fields.append(capitalize(snakeToCamelCase(name))).append("-").append(value).append(" ");
        });
        return List.of(new DataGenMethod("generateOnlyRequired" + modelName, fields.toString().trim()));
    }

    private List<DataGenMethod> generateIncorrectMethods() {
        List<DataGenMethod> methods = new ArrayList<>();
        schema.getProperties().forEach((name, prop) -> {
            if (hasConstraints(prop)) {
                String incorrectValue = getIncorrectFieldValue(prop);
                StringBuilder fields = new StringBuilder();
                schema.getProperties().forEach((otherName, otherProp) -> {
                    String value = name.equals(otherName) ? incorrectValue : getFieldValue(otherProp, true);
                    fields.append(capitalize(snakeToCamelCase(otherName))).append("-").append(value).append(" ");
                });
                methods.add(new DataGenMethod(
                        "generateIncorrect" + modelName + "By" + capitalize(name),
                        fields.toString().trim()
                ));
            }
        });
        return methods;
    }
}
