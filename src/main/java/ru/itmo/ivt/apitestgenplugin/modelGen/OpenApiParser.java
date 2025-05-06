package ru.itmo.ivt.apitestgenplugin.modelGen;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SystemProperties;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.modelGen.converters.MetadataToPojoConverter;
import ru.itmo.ivt.apitestgenplugin.modelGen.file.PackagesManager;
import ru.itmo.ivt.apitestgenplugin.modelGen.schema.JsonSchemaCreator;

import java.io.File;
import java.util.List;
import java.util.Map;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createPsiDirectoryFromPath;
import static ru.itmo.ivt.apitestgenplugin.util.OpenApiUtils.getModelsByControllers;

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
    public GenerationContext fillContext(GenerationContext context) {
        File outputDirectoryFile = new File(outputDirectory);
        SwaggerParseResult result = OPEN_API_PARSER.readLocation(filePath, null, PARSE_OPTIONS);

        // generate data models
        String resultComponentsSchema = jsonSchemaCreator.generateModelJsonSchema(result.getOpenAPI());
        pojoConverter.convertMetadataToPojo(resultComponentsSchema, outputDirectoryFile, outputPackage, DEFAULT_ROOT_NAME);
        refreshFilesystem(outputDirectory);
        packagesManager.clearModelFile(getRootPsiFile(context.getProject()));

        // manage data models by packages
        Map<String, List<String>> modelsByControllers = getModelsByControllers(result.getOpenAPI().getPaths());
        // Map<String, PsiFile> models = packagesManager.splitModelFilesByDirectories(modelsByControllers, getModelsPackagePath(), context.getProject());

        context.setOpenAPI(result.getOpenAPI());
        //context.setModelFilesByPackages(models);
        return context;
    }

    private void refreshFilesystem(String path) {
        VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        if (vFile != null) {
            vFile.refresh(false, true);
        }
    }

    private PsiFile getRootPsiFile(@NotNull Project project) {
        return ReadAction.compute(() -> {
            ProgressManager.checkCanceled();
            PsiDirectory outputDir = createPsiDirectoryFromPath(project, outputDirectory);
            if (outputDir == null) {
                throw new RuntimeException("Output directory not found: " + outputDirectory);
            }
            PsiDirectory packageDir = outputDir.findSubdirectory(outputPackage);
            if (packageDir == null) {
                throw new RuntimeException("Package directory not found: " + outputPackage);
            }
            return packageDir.findFile(getRootFileName());
        });
    }

    private String getRootFileName() {
        return DEFAULT_ROOT_NAME + DEFAULT_ROOT_PREFIX;
    }

    private String getModelsPackagePath() {
        return outputDirectory + SystemProperties.getFileSeparator() + outputPackage;
    }
}
