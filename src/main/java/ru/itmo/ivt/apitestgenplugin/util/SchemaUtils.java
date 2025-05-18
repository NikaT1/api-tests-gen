package ru.itmo.ivt.apitestgenplugin.util;

import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import lombok.experimental.UtilityClass;
import ru.itmo.ivt.apitestgenplugin.model.openapi.TypeWithImport;

import java.math.BigDecimal;
import java.util.List;

@UtilityClass
public class SchemaUtils {
    private static final List<String> NO_IMPORT = List.of();
    private static final String DEFAULT_MEDIA_TYPE = "application/json";

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
        } else if (requestBody.getContent() != null && requestBody.getContent().get(DEFAULT_MEDIA_TYPE) != null) {
            type = extractObjectType(requestBody.getContent().get(DEFAULT_MEDIA_TYPE).getSchema().get$ref(), modelsPackage);
        }
        return type;
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

    public static boolean hasConstraints(Schema<?> schema) {
        if (schema == null) {
            return false;
        }
        if (schema instanceof StringSchema) {
            StringSchema stringSchema = (StringSchema) schema;
            return stringSchema.getMaxLength() != null
                    || stringSchema.getMinLength() != null
                    || stringSchema.getPattern() != null
                    || stringSchema.getFormat() != null
                    || (stringSchema.getEnum() != null && !stringSchema.getEnum().isEmpty());
        }
        else if (schema instanceof IntegerSchema) {
            IntegerSchema intSchema = (IntegerSchema) schema;
            return intSchema.getMaximum() != null
                    || intSchema.getMinimum() != null
                    || intSchema.getExclusiveMaximum() != null
                    || intSchema.getExclusiveMinimum() != null
                    || intSchema.getMultipleOf() != null;
        }
        else if (schema instanceof NumberSchema) {
            NumberSchema numSchema = (NumberSchema) schema;
            return numSchema.getMaximum() != null
                    || numSchema.getMinimum() != null
                    || numSchema.getExclusiveMaximum() != null
                    || numSchema.getExclusiveMinimum() != null
                    || numSchema.getMultipleOf() != null;
        }
        else if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            return arraySchema.getMaxItems() != null
                    || arraySchema.getMinItems() != null
                    || arraySchema.getUniqueItems() != null
                    || hasConstraints(arraySchema.getItems());
        }
        else if (schema instanceof ObjectSchema) {
            ObjectSchema objSchema = (ObjectSchema) schema;
            return objSchema.getMaxProperties() != null
                    || objSchema.getMinProperties() != null
                    || (objSchema.getProperties() != null && !objSchema.getProperties().isEmpty())
                    || (objSchema.getRequired() != null && !objSchema.getRequired().isEmpty());
        }
        else if (schema instanceof BooleanSchema) {
            return false;
        }
        else if (schema instanceof DateTimeSchema
                || schema instanceof DateSchema
                || schema instanceof PasswordSchema
                || schema instanceof EmailSchema
                || schema instanceof UUIDSchema) {
            return schema.getPattern() != null
                    || schema.getEnum() != null
                    || schema.getNullable() != null;
        }
        else if (schema instanceof ComposedSchema composedSchema) {
            return (composedSchema.getAllOf() != null && !composedSchema.getAllOf().isEmpty())
                    || (composedSchema.getAnyOf() != null && !composedSchema.getAnyOf().isEmpty())
                    || (composedSchema.getOneOf() != null && !composedSchema.getOneOf().isEmpty())
                    || (composedSchema.getNot() != null);
        }

        return false;
    }

    public static String getFieldValue(Schema<?> schema, boolean correct) {
        if (schema == null) {
            return "null";
        }

        if (schema instanceof StringSchema) {
            StringSchema stringSchema = (StringSchema) schema;
            if (!correct) {
                return "null";
            }
            if (stringSchema.getEnum() != null && !stringSchema.getEnum().isEmpty()) {
                return "\"" + stringSchema.getEnum().get(0) + "\"";
            }
            int length = stringSchema.getMaxLength() != null ?
                    Math.max(1, stringSchema.getMaxLength() / 2) : 10;
            return "randomString(" + length + ")";
        }
        else if (schema instanceof IntegerSchema) {
            if (!correct) {
                return "null";
            }
            IntegerSchema intSchema = (IntegerSchema) schema;
            if (intSchema.getEnum() != null && !intSchema.getEnum().isEmpty()) {
                return intSchema.getEnum().get(0).toString();
            }
            if (intSchema.getMaximum() != null && intSchema.getMinimum() != null) {
                return "randomInt(" + intSchema.getMinimum() + "," + intSchema.getMaximum() + ")";
            }
            if (intSchema.getMaximum() != null) {
                return "randomInt(" + intSchema.getMaximum().add(BigDecimal.TEN.negate()) + "," + intSchema.getMaximum() + ")";
            }
            if (intSchema.getMinimum() != null) {
                return "randomInt(" + intSchema.getMinimum() + "," + intSchema.getMinimum().add(BigDecimal.TEN) + ")";
            }
            return "randomInt()";
        }
        else if (schema instanceof NumberSchema) {
            if (!correct) {
                return "null";
            }
            NumberSchema numSchema = (NumberSchema) schema;
            if (numSchema.getEnum() != null && !numSchema.getEnum().isEmpty()) {
                return numSchema.getEnum().get(0).toString();
            }
            return "randomDouble()";
        }
        else if (schema instanceof BooleanSchema) {
            return correct ? "randomBoolean()" : "null";
        }
        else if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            if (!correct) {
                return "null";
            }
            return "java.util.Arrays.asList(" + getFieldValue(arraySchema.getItems(), true) + ")";
        }
        else if (schema instanceof ObjectSchema) {
            return correct ? schema.getName() + "Generator.generateCorrect" + schema.getName() + "()" : "null";
        }
        else if (schema instanceof DateTimeSchema) {
            return correct ? "generateDate()" : "\"invalid-date\"";
        }
        else if (schema instanceof DateSchema) {
            return correct ? "generateDate()" : "\"invalid-date\"";
        }
        else if (schema instanceof EmailSchema) {
            return correct ? "\"user@example.com\"" : "\"invalid-email\"";
        }
        else if (schema instanceof UUIDSchema) {
            return correct ? "UUID.randomUUID().toString()" : "\"invalid-uuid\"";
        }
        else if (schema instanceof PasswordSchema) {
            return correct ? "\"Password1!\"" : "\"weak\"";
        }
        return "null";
    }

    public static String getIncorrectFieldValue(Schema<?> schema) {
        if (schema == null) {
            return "null";
        }

        if (schema instanceof StringSchema) {
            StringSchema stringSchema = (StringSchema) schema;
            if (stringSchema.getEnum() != null && !stringSchema.getEnum().isEmpty()) {
                return "\"invalid-enum-value\"";
            }
            if (stringSchema.getMaxLength() != null) {
                return "randomString(" + (stringSchema.getMaxLength() + 10) + ")";
            }
            if (stringSchema.getPattern() != null) {
                return "\"does-not-match-pattern\"";
            }
            return "null";
        }
        else if (schema instanceof IntegerSchema) {
            IntegerSchema intSchema = (IntegerSchema) schema;
            if (intSchema.getEnum() != null && !intSchema.getEnum().isEmpty()) {
                return "999999999";
            }
            if (intSchema.getMaximum() != null) {
                return String.valueOf(intSchema.getMaximum().add(new BigDecimal("1")));
            }
            if (intSchema.getMinimum() != null) {
                return String.valueOf(intSchema.getMinimum().add(new BigDecimal("-1")));
            }
            return "null";
        }
        else if (schema instanceof NumberSchema) {
            NumberSchema numSchema = (NumberSchema) schema;
            if (numSchema.getEnum() != null && !numSchema.getEnum().isEmpty()) {
                return "999999.99";
            }
            if (numSchema.getMaximum() != null) {
                return String.valueOf(numSchema.getMaximum().add(new BigDecimal("1.0")));
            }
            if (numSchema.getMinimum() != null) {
                return String.valueOf(numSchema.getMinimum().add(new BigDecimal("-1.0")));
            }
            return "null";
        }
        else if (schema instanceof BooleanSchema) {
            return "\"not-a-boolean\"";
        }
        else if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            if (arraySchema.getMaxItems() != null) {
                return "java.util.Collections.nCopies(" + (arraySchema.getMaxItems() + 5) +
                        ", " + getFieldValue(arraySchema.getItems(), true) + ")";
            }
            return "null";
        }
        else if (schema instanceof DateTimeSchema) {
            return "\"2023-13-01T25:61:61Z\"";
        }
        else if (schema instanceof DateSchema) {
            return "\"2023-13-01\"";
        }
        else if (schema instanceof EmailSchema) {
            return "\"not-an-email\"";
        }
        else if (schema instanceof UUIDSchema) {
            return "\"not-a-uuid\"";
        }
        else if (schema instanceof PasswordSchema) {
            return "\"short\"";
        }

        return "null";
    }

    private static TypeWithImport extractObjectType(String ref, String modelsPackage) {
        String modelName = ref.substring(ref.lastIndexOf('/') + 1);
        return new TypeWithImport(modelName, List.of(getModelImport(modelsPackage, modelName)));
    }

    private static String getModelImport(String modelPackage, String modelName) {
        return modelPackage + "." + modelName;
    }
}
