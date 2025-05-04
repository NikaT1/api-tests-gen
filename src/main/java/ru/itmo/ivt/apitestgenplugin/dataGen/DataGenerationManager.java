package ru.itmo.ivt.apitestgenplugin.dataGen;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import io.swagger.v3.oas.models.OpenAPI;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

import java.util.Map;

import static ru.itmo.ivt.apitestgenplugin.dataGen.DataGenFileGenerator.createModelGeneratorFile;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createPsiDirectoryFromPath;

public class DataGenerationManager {
    private static final String DEFAULT_GENERATORS_PATH = "/main/java/models/generators";
    private static final String DEFAULT_GENERATORS_PACKAGE = "models.generators";

    public void generateDataFiles(PsiDirectory srcDir, GenerationContext context) {
        createModelGenerators(srcDir, context);
    }

    private void createModelGenerators(PsiDirectory srcDir, GenerationContext context) {
        PsiDirectory directory = createPsiDirectoryFromPath(context.getProject(),
                srcDir.getVirtualFile().getCanonicalPath() + DEFAULT_GENERATORS_PATH);
        Map<String, PsiFile> models = context.getModelFilesByPackages();
        OpenAPI openAPI = context.getOpenAPI();
        assert openAPI != null;

        models.values().forEach(psiFile -> {
            String fileName = psiFile.getName();
            String className = fileName.substring(0, fileName.lastIndexOf('.'));
            DataGenMethodManager manager = new DataGenMethodManager(className, openAPI.getComponents().getSchemas().get(className));
            createModelGeneratorFile(context.getProject(), directory, DEFAULT_GENERATORS_PACKAGE, manager);
        });
    }
}
