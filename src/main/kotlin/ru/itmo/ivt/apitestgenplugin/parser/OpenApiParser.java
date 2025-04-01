package ru.itmo.ivt.apitestgenplugin.parser;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SystemProperties;
import ru.itmo.ivt.apitestgenplugin.parser.converters.MetadataToPojoConverter;
import ru.itmo.ivt.apitestgenplugin.parser.file.PackagesManager;
import ru.itmo.ivt.apitestgenplugin.parser.schema.JsonSchemaCreator;

import java.io.File;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class OpenApiParser {
    private final static String DEFAULT_ROOT_NAME = "Root";
    private final static String DEFAULT_ROOT_PREFIX = ".java";
    private final static OpenAPIParser OPEN_API_PARSER = new OpenAPIParser();
    private final static ParseOptions PARSE_OPTIONS = new ParseOptions();

    static {
        PARSE_OPTIONS.setResolve(false);
        PARSE_OPTIONS.setResolveFully(false);
    }

    private final MetadataToPojoConverter pojoConverter;
    private final JsonSchemaCreator jsonSchemaCreator;
    private final PackagesManager packagesManager;
    private final String filePath;
    private final String outputDirectory;
    private final String outputPackage;

    @SneakyThrows
    // TODO добавить опцию по переносу всего в schemas
    public void fillContext() {
        File outputDirectoryFile = new File(outputDirectory);
        SwaggerParseResult result = OPEN_API_PARSER.readLocation(filePath, null, PARSE_OPTIONS);

        // generate data models
        String resultComponentsSchema = jsonSchemaCreator.generateModelJsonSchema(result.getOpenAPI());
        pojoConverter.convertMetadataToPojo(resultComponentsSchema, outputDirectoryFile, outputPackage, DEFAULT_ROOT_NAME);
        packagesManager.clearModelFile(new File(outputDirectory, getRootFilePath()));

        // manage data models by packages
        Map<String, List<String>> modelsByControllers = packagesManager.getModelsByControllers(result.getOpenAPI().getPaths());
        packagesManager.splitModelFilesByDirectories(modelsByControllers, getModelsPackagePath());
    }

    private String getRootFilePath() {
        return outputPackage + SystemProperties.getFileSeparator() + DEFAULT_ROOT_NAME + DEFAULT_ROOT_PREFIX;
    }

    private String getModelsPackagePath() {
        return outputDirectory + SystemProperties.getFileSeparator() + outputPackage;
    }
}
