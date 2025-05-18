package ru.itmo.ivt.apitestgenplugin.dataGen;

import com.intellij.psi.PsiDirectory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import ru.itmo.ivt.apitestgenplugin.dataGen.generators.DataGenMethodGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

import java.util.Map;

import static ru.itmo.ivt.apitestgenplugin.dataGen.generators.DataGenFileGenerator.createModelGeneratorFile;
import static ru.itmo.ivt.apitestgenplugin.dataGen.generators.PrimitiveDataGenFileGenerator.createPrimitiveGeneratorFile;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createPsiDirectoryFromPath;

public class DataGenerationManager {
    private static final String DEFAULT_GENERATORS_PATH = "/main/java/models/generators";
    private static final String DEFAULT_GENERATORS_PACKAGE = "models.generators";

    public void generateDataFiles(PsiDirectory srcDir, GenerationContext context) {
        PsiDirectory directory = createPsiDirectoryFromPath(context.getProject(),
                srcDir.getVirtualFile().getCanonicalPath() + DEFAULT_GENERATORS_PATH);
        createModelGenerators(directory, context);
        createPrimitiveGeneratorFile(context.getProject(), directory);
    }

    private void createModelGenerators(PsiDirectory directory, GenerationContext context) {
        Map<String, Schema> models = context.getModelSchemasByName();
        assert models != null;
        OpenAPI openAPI = context.getOpenAPI();
        assert openAPI != null;

        models.keySet().forEach(className -> {
            DataGenMethodGenerator generator = new DataGenMethodGenerator(className,
                    openAPI.getComponents().getSchemas().get(className));
            createModelGeneratorFile(context.getProject(), directory, DEFAULT_GENERATORS_PACKAGE, generator);
        });
    }
}
