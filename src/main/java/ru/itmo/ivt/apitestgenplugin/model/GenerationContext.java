package ru.itmo.ivt.apitestgenplugin.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GenerationContext {
    private Project project;
    private UserInput userInput;
    private Map<String, PsiFile> modelFilesByPackages = new HashMap<>();
    private List<PsiFile> testFiles = new ArrayList<>();
    private Map<String, PsiFile> clientFiles = new HashMap<>();
    private Map<String, List<String>> methodNamesByClients = new HashMap<>();
    private OpenAPI openAPI;
}
