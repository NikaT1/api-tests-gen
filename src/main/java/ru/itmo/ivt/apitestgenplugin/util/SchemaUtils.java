package ru.itmo.ivt.apitestgenplugin.util;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import lombok.experimental.UtilityClass;
import ru.itmo.ivt.apitestgenplugin.model.openapi.TypeWithImport;

import java.util.List;

@UtilityClass
public class SchemaUtils {
    private static final List<String> NO_IMPORT = List.of();

    public static TypeWithImport mapSchemaToJavaType(Schema<?> schema, String modelsPackage) {
        if (schema == null) {
            return new TypeWithImport("Object", NO_IMPORT);
        }

        if (schema.get$ref() != null) {
            return extractObjectType(schema.get$ref(), modelsPackage);
        }

        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            TypeWithImport type = mapSchemaToJavaType(items, modelsPackage);
            type.importPath().add("java.util.List");
            return new TypeWithImport("List<" + type.type() + ">", type.importPath());
        }

        if (schema.getAdditionalProperties() != null) {
            if (schema.getAdditionalProperties() instanceof Schema) {
                Schema<?> valueSchema = (Schema<?>) schema.getAdditionalProperties();
                TypeWithImport type = mapSchemaToJavaType(valueSchema, modelsPackage);
                type.importPath().add("java.util.Map");
                return new TypeWithImport("Map<String, " + type.type() + ">", type.importPath());
            }
            return new TypeWithImport("Map<String, Object>", List.of("java.util.Map"));
        }

        return mapOpenApiTypeToJavaType(schema.getType(), schema.getFormat());
    }

    public static TypeWithImport extractObjectTypeFromBody(RequestBody requestBody, String modelsPackage) {
        TypeWithImport type = new TypeWithImport("Object", NO_IMPORT);
        if (requestBody.get$ref() != null) {
            type = extractObjectType(requestBody.get$ref(), modelsPackage);
        }
        return type;
    }

    private static TypeWithImport extractObjectType(String ref, String modelsPackage) {
        String modelName = ref.substring(ref.lastIndexOf('/') + 1);
        return new TypeWithImport(modelName, List.of(getModelImport(modelsPackage, modelName)));
    }

    public static TypeWithImport mapOpenApiTypeToJavaType(String openApiType, String format) {
        if (openApiType == null) return new TypeWithImport("Object", NO_IMPORT);

        return switch (openApiType.toLowerCase()) {
            case SchemaTypeUtil.STRING_TYPE -> {
                if (SchemaTypeUtil.DATE_FORMAT.equals(format)) yield new TypeWithImport("LocalDate", List.of("java.time.LocalDate"));
                if (SchemaTypeUtil.DATE_TIME_FORMAT.equals(format)) yield new TypeWithImport("LocalDateTime", List.of("java.time.LocalDateTime"));
                if (SchemaTypeUtil.BYTE_FORMAT.equals(format)) yield new TypeWithImport("byte[]", NO_IMPORT);
                yield new TypeWithImport("String", NO_IMPORT);
            }
            case SchemaTypeUtil.INTEGER_TYPE -> {
                if (SchemaTypeUtil.INTEGER32_FORMAT.equals(format)) yield new TypeWithImport("Integer", NO_IMPORT);
                yield new TypeWithImport("Long", NO_IMPORT);
            }
            case SchemaTypeUtil.NUMBER_TYPE -> {
                if (SchemaTypeUtil.FLOAT_FORMAT.equals(format)) yield new TypeWithImport("Float", NO_IMPORT);
                yield new TypeWithImport("Double", NO_IMPORT);
            }
            case SchemaTypeUtil.BOOLEAN_TYPE -> new TypeWithImport("Boolean", NO_IMPORT);
            default -> new TypeWithImport("Object", NO_IMPORT);
        };
    }

    private static String getModelImport(String modelPackage, String modelName) {
        return modelPackage + "." + modelName;
    }
}
