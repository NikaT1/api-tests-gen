package ru.itmo.ivt.apitestgenplugin.modelGen;

import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.modelGen.converters.impl.JsonMetadataToPojoConverter;
import ru.itmo.ivt.apitestgenplugin.modelGen.file.impl.PackagesManagerImpl;
import ru.itmo.ivt.apitestgenplugin.modelGen.schema.impl.JsonSchemaCreatorImpl;

import java.io.File;

public class ParserManager {
    private static final String OUTPUT_DIRECTORY = "/main/java";
    private static final String MODEL_PACKAGE_NAME = "models";

    public void prepareGeneratorContext(String targetDir, GenerationContext context) {
        new OpenApiParser(new JsonMetadataToPojoConverter(),
                new JsonSchemaCreatorImpl(),
                new PackagesManagerImpl(),
                context.getUserInput().openApiPath(),
                targetDir + OUTPUT_DIRECTORY,
                MODEL_PACKAGE_NAME).fillContext(context);
        context.setModelConfiguration(ConfigParser.parseModelConfig(new File(context.getUserInput().dataConfigPath())));
        context.setTestConfiguration(ConfigParser.parseTestConfig(new File(context.getUserInput().testConfigPath())));
    }
}
