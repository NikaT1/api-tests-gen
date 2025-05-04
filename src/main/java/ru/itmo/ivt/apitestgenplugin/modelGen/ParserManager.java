package ru.itmo.ivt.apitestgenplugin.modelGen;

import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.UserInput;
import ru.itmo.ivt.apitestgenplugin.modelGen.converters.impl.JsonMetadataToPojoConverter;
import ru.itmo.ivt.apitestgenplugin.modelGen.file.impl.PackagesManagerImpl;
import ru.itmo.ivt.apitestgenplugin.modelGen.schema.impl.JsonSchemaCreatorImpl;

import java.io.File;

public class ParserManager {
    private static final String OUTPUT_DIRECTORY = "/main/java";
    private static final String MODEL_PACKAGE_NAME = "models";

    public void prepareGeneratorContext(UserInput userInput, String targetDir, GenerationContext context) {
        new OpenApiParser(new JsonMetadataToPojoConverter(),
                new JsonSchemaCreatorImpl(),
                new PackagesManagerImpl(),
                userInput.openApiPath(),
                targetDir + OUTPUT_DIRECTORY,
                MODEL_PACKAGE_NAME).fillContext(context);
        context.setModelConfiguration(ConfigParser.parseModelConfig(new File(userInput.dataConfigPath())));
        context.setTestConfiguration(ConfigParser.parseTestConfig(new File(userInput.testConfigPath())));
    }
}
