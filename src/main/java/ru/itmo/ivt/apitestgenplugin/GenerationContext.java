package ru.itmo.ivt.apitestgenplugin;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
public class GenerationContext {
    private Project project;
    private List<File> modelFiles;
    private List<PsiFile> testFiles;
    private List<PsiFile> clientFiles;
    private OpenAPI openAPI;
}
