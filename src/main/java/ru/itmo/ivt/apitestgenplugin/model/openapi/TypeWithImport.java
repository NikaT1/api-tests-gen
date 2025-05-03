package ru.itmo.ivt.apitestgenplugin.model.openapi;

import java.util.List;

public record TypeWithImport(String type, List<String> importPath) {
}
