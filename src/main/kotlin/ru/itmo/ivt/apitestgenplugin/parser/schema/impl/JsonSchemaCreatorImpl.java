package ru.itmo.ivt.apitestgenplugin.parser.schema.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import ru.itmo.ivt.apitestgenplugin.parser.schema.JsonSchemaCreator;

import java.util.Map;
import java.util.stream.Collectors;

import static ru.itmo.ivt.apitestgenplugin.parser.config.ParserObjectMapperConfig.getObjectMapper;

public class JsonSchemaCreatorImpl implements JsonSchemaCreator {
    @Override
    public String generateModelJsonSchema(OpenAPI openAPI) throws JsonProcessingException {
        Map<String, Schema> nameToSchemaMap = openAPI.getComponents().getSchemas();
        StringBuilder definitionsSchema = generateDefinitions(nameToSchemaMap);
        return generateGlobalObjectProperties(definitionsSchema, nameToSchemaMap).toString();
    }

    private StringBuilder generateDefinitions(Map<String, Schema> nameToSchemaMap) throws JsonProcessingException {
        StringBuilder definitionsSchema = new StringBuilder("{\"$defs\": {");
        boolean firstAccess = true;
        for (Map.Entry<String, Schema> nameToSchema : nameToSchemaMap.entrySet()) {
            if (!firstAccess) {
                definitionsSchema.append(",");
            }
            firstAccess = false;
            nameToSchema.getValue().title(nameToSchema.getKey());
            String jsonSchema = getObjectMapper().writeValueAsString(nameToSchema.getValue()).replace("components/schemas", "$defs");
            definitionsSchema.append("\"").append(nameToSchema.getKey()).append("\":").append(jsonSchema);
        }
        return definitionsSchema.append("},");
    }

    private StringBuilder generateGlobalObjectProperties(StringBuilder jsonSchema, Map<String, Schema> nameToSchemaMap) {
        return jsonSchema.append("\"type\": \"object\",\"properties\": { ")
                .append(nameToSchemaMap.keySet().stream()
                        .map(schema -> "\"" + schema + "\":" + "{ \"$ref\": \"#/$defs/" + schema + "\"}")
                        .collect(Collectors.joining(", ", "", "")))
                .append("}}");
    }
}
