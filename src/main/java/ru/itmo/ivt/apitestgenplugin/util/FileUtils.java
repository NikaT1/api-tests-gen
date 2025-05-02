package ru.itmo.ivt.apitestgenplugin.util;

import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class FileUtils {
    public static PsiDirectory getSrcDirectory(@NotNull Project project) {
        return ReadAction.compute(() -> {
            VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
            if (projectDir == null) return null;
            VirtualFile srcDir = projectDir.findChild("src");
            if (srcDir != null && srcDir.isDirectory()) {
                return PsiManager.getInstance(project).findDirectory(srcDir);
            }
            return null;
        });
    }

    public static void fixImports(Project project, PsiFile file) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            new OptimizeImportsProcessor(project, file).run();
            new ReformatCodeProcessor(project, file, null, false).run();
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
