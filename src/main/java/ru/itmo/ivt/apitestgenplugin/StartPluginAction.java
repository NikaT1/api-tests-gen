package ru.itmo.ivt.apitestgenplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import ru.itmo.ivt.apitestgenplugin.parser.ParserManager;

public class StartPluginAction extends AnAction {
    private final ParserManager parserManager = new ParserManager();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        InputDialog dialog = new InputDialog(project);
        dialog.show();
        ApplicationManager.getApplication().runWriteAction(() -> {
            if (dialog.isOK()) {
                String specFilePath = dialog.getOpenApiSpecTextField().getText();
                //String targetDir = e.getData(PlatformDataKeys.VIRTUAL_FILE).getParent().getCanonicalPath();
                String targetDir = "C:/Users/пользователь/IdeaProjects/api-tests-plugin/src"; // only for testing
                parserManager.prepareGeneratorContext(specFilePath, targetDir, project);
            }
        });
    }
}
