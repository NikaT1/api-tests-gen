package ru.itmo.ivt.apitestgenplugin;

import ru.itmo.ivt.apitestgenplugin.parser.OpenApiParser;
import ru.itmo.ivt.apitestgenplugin.parser.converters.impl.JsonMetadataToPojoConverter;
import ru.itmo.ivt.apitestgenplugin.parser.file.impl.PackagesManagerImpl;
import ru.itmo.ivt.apitestgenplugin.parser.schema.impl.JsonSchemaCreatorImpl;

public class Main {
    public static void main(String[] args) {
        new OpenApiParser(new JsonMetadataToPojoConverter(),
                new JsonSchemaCreatorImpl(),
                new PackagesManagerImpl(),
                "collection.json",
                "src/test/java",
                "models").fillContext();
    }
}