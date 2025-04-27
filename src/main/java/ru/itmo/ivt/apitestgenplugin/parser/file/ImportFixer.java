package ru.itmo.ivt.apitestgenplugin.parser.file;

import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

public class ImportFixer {
    public static void fixImports(Project project, VirtualFile file) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
                // Оптимизировать импорты (удалить неиспользуемые, добавить недостающие)
                new OptimizeImportsProcessor(project, psiFile).run();

                // Отформатировать код (опционально)
                new ReformatCodeProcessor(project, psiFile, null, false).run();
            }
        });
    }
}
