package ru.itmo.ivt.apitestgenplugin.util;

import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {
    public static void fixImports(Project project, VirtualFile file) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiFile psiFile = psiManager.findFile(file);
            assert psiFile != null;
            new OptimizeImportsProcessor(project, psiFile).run();
            new ReformatCodeProcessor(project, psiFile, null, false).run();
        });
    }

    public static PsiDirectory createControllerDirectory(Project project,
                                                         PsiDirectory baseDir,
                                                         String controllerName) {
        try {
            PsiDirectory existingDir = baseDir.findSubdirectory(controllerName);
            if (existingDir != null) return existingDir;

            return WriteCommandAction.runWriteCommandAction(project, (Computable<PsiDirectory>) () -> {
                return baseDir.createSubdirectory(controllerName);
            });
        } catch (Exception e) {
            return null;
        }
    }

    public static String getPackageName(PsiDirectory baseTestDir, String controllerName) {
        String basePackage = JavaDirectoryService.getInstance()
                .getPackage(baseTestDir)
                .getQualifiedName();
        return basePackage + "." + controllerName.toLowerCase();
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
