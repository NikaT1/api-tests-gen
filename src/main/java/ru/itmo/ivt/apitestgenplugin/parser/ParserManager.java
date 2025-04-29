package ru.itmo.ivt.apitestgenplugin.parser;

import com.intellij.openapi.project.Project;
import ru.itmo.ivt.apitestgenplugin.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.parser.converters.impl.JsonMetadataToPojoConverter;
import ru.itmo.ivt.apitestgenplugin.parser.file.impl.PackagesManagerImpl;
import ru.itmo.ivt.apitestgenplugin.parser.schema.impl.JsonSchemaCreatorImpl;

public class ParserManager {
    private static final String OUTPUT_DIRECTORY = "/test/java";
    private static final String MODEL_PACKAGE_NAME = "models";

    public GenerationContext prepareGeneratorContext(String specFilePath, String targetDir, Project project) {
        return new OpenApiParser(new JsonMetadataToPojoConverter(),
                new JsonSchemaCreatorImpl(),
                new PackagesManagerImpl(),
                specFilePath,
                targetDir + OUTPUT_DIRECTORY,
                MODEL_PACKAGE_NAME).fillContext(project);
    }
}
