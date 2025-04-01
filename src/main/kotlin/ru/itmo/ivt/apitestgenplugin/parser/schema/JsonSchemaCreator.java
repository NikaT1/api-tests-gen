package ru.itmo.ivt.apitestgenplugin.parser.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.OpenAPI;

public interface JsonSchemaCreator {
    String generateModelJsonSchema(OpenAPI openAPI) throws JsonProcessingException;
}
