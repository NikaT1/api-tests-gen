package ru.itmo.ivt.apitestgenplugin.model.openapi;

public record ApiMethodParam(
        TypeWithImport type,
        String name,
        boolean isPathParam,
        boolean isQueryParam,
        boolean isBodyParam) {
}
