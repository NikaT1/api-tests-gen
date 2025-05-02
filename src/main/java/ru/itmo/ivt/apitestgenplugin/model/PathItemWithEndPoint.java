package ru.itmo.ivt.apitestgenplugin.model;

import io.swagger.v3.oas.models.PathItem;

public record PathItemWithEndPoint(PathItem pathItem, String path) {
}
