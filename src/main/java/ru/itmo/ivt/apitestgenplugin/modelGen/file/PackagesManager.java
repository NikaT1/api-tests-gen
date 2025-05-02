package ru.itmo.ivt.apitestgenplugin.modelGen.file;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import java.util.List;
import java.util.Map;

public interface PackagesManager {
    Map<String, PsiFile> splitModelFilesByDirectories(Map<String, List<String>> modelsByControllers, String directory, Project project);
    void clearModelFile(PsiFile psiFile);
}
