package ru.itmo.ivt.apitestgenplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import ru.itmo.ivt.apitestgenplugin.codeGen.TestGenerationManager;
import ru.itmo.ivt.apitestgenplugin.parser.ParserManager;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createPsiDirectoryFromPath;

public class StartPluginAction extends AnAction {
    private final ParserManager parserManager = new ParserManager();
    private final TestGenerationManager testGenerationManager = new TestGenerationManager();

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
                PsiDirectory srcDir = createPsiDirectoryFromPath(project, targetDir);
                GenerationContext context = parserManager.prepareGeneratorContext(specFilePath, targetDir, project);
                testGenerationManager.generateClientsAndTests(srcDir, context);
            }
        });
    }
}
