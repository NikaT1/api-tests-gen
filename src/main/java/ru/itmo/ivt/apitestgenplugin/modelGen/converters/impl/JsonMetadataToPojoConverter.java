package ru.itmo.ivt.apitestgenplugin.modelGen.converters.impl;

import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;
import ru.itmo.ivt.apitestgenplugin.modelGen.converters.MetadataToPojoConverter;

import java.io.File;
import java.io.IOException;

public class JsonMetadataToPojoConverter implements MetadataToPojoConverter {

    private static final GenerationConfig GENERATION_CONFIG = new DefaultGenerationConfig() {
        @Override
        public boolean isGenerateBuilders() {
            return true;
        }

        @Override
        public SourceType getSourceType() {
            return SourceType.JSONSCHEMA;
        }

        @Override
        public boolean isUseTitleAsClassname(){
            return true;
        }
        @Override
        public InclusionLevel getInclusionLevel() {
            return InclusionLevel.ALWAYS;
        }
    };

    public void convertMetadataToPojo(String metadata, File outputDirectory, String outputPackage, String javaClassName) throws IOException {
        JCodeModel jcodeModel = new JCodeModel();

        SchemaMapper mapper = new SchemaMapper(new RuleFactory(GENERATION_CONFIG, new Jackson2Annotator(GENERATION_CONFIG), new SchemaStore()), new SchemaGenerator());
        mapper.generate(jcodeModel, javaClassName, outputPackage, metadata);

        jcodeModel.build(outputDirectory);
    }
}
