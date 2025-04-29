package ru.itmo.ivt.apitestgenplugin.util;

import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import lombok.experimental.UtilityClass;

import java.io.File;

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

    public static PsiDirectory createSubDirectory(Project project,
                                                  PsiDirectory baseDir,
                                                  String dir) {
        try {
            PsiDirectory existingDir = baseDir.findSubdirectory(dir);
            if (existingDir != null) return existingDir;

            return WriteCommandAction.runWriteCommandAction(project, (Computable<PsiDirectory>) () -> {
                return baseDir.createSubdirectory(dir);
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

    public static PsiDirectory createPsiDirectoryFromPath(Project project, String targetDir) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(targetDir);

        if (virtualFile == null) {
            virtualFile = createDirectory(targetDir);
            if (virtualFile == null) {
                throw new RuntimeException("Failed to create directory: " + targetDir);
            }
        }

        PsiManager psiManager = PsiManager.getInstance(project);
        return psiManager.findDirectory(virtualFile);
    }

    private static VirtualFile createDirectory(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists() && !dir.mkdirs()) {
                return null;
            }
            return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        } catch (Exception e) {
            return null;
        }
    }
}
