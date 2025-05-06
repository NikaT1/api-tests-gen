package ru.itmo.ivt.apitestgenplugin.util;

import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class FileUtils {
    public static PsiFile createFile(@NotNull Project project,
                                     @NotBlank String fileContent,
                                     @NotNull PsiDirectory directory,
                                     @NotBlank String fileName,
                                     @NotNull FileType fileType) {
        return WriteCommandAction.writeCommandAction(project)
                .compute(() -> ReadAction.compute(() -> {
                    ProgressManager.checkCanceled();
                    PsiFileFactory factory = PsiFileFactory.getInstance(project);
                    PsiFile psiFile = factory.createFileFromText(
                            fileName,
                            fileType,
                            fileContent
                    );
                    PsiFile prevFile = directory.findFile(fileName);
                    return prevFile != null ? prevFile : (PsiFile) directory.add(psiFile);
                }));
    }

    public static PsiDirectory getSrcDirectory(@NotNull Project project) {
        return ReadAction.compute(() -> {
            ProgressManager.checkCanceled();
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
        ApplicationManager.getApplication().invokeLater(() ->
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    ProgressManager.checkCanceled();
                    new OptimizeImportsProcessor(project, file).run();
                    new ReformatCodeProcessor(project, file, null, false).run();
                })
        );
    }

    public static PsiDirectory createNestedDirectories(@NotNull Project project,
                                                       @NotNull PsiDirectory baseDir,
                                                       @NotBlank String path) {
        return ReadAction.compute(() -> {
            String[] parts = path.split("/");
            PsiDirectory currentDir = baseDir;

            for (String dirName : parts) {
                if (dirName.isEmpty()) continue;
                ProgressManager.checkCanceled();
                PsiDirectory checkDir = currentDir;

                currentDir = WriteCommandAction.writeCommandAction(project).compute(() -> {
                    PsiDirectory existingDir = checkDir.findSubdirectory(dirName);
                    return existingDir != null ? existingDir : checkDir.createSubdirectory(dirName);
                });

                if (currentDir == null) break;
            }
            return currentDir;
        });
    }

    public static PsiDirectory createSubDirectory(@NotNull Project project,
                                                  @NotNull PsiDirectory baseDir,
                                                  @NotBlank String dir) {
        return ReadAction.compute(() ->
                WriteCommandAction.writeCommandAction(project).compute(() -> {
                    ProgressManager.checkCanceled();
                    PsiDirectory existingDir = baseDir.findSubdirectory(dir);
                    return existingDir != null ? existingDir : baseDir.createSubdirectory(dir);
                })
        );
    }

    public static PsiDirectory createPsiDirectoryFromPath(Project project, String targetDir) {
        return ReadAction.compute(() -> {
            ProgressManager.checkCanceled();
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(targetDir);

            if (virtualFile == null) {
                virtualFile = WriteCommandAction.writeCommandAction(project)
                        .compute(() -> createDirectory(targetDir));
                if (virtualFile == null) {
                    throw new RuntimeException("Failed to create directory: " + targetDir);
                }
            }

            return PsiManager.getInstance(project).findDirectory(virtualFile);
        });
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