package ru.itmo.ivt.apitestgenplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import ru.itmo.ivt.apitestgenplugin.codeGen.TestGenerationManager;
import ru.itmo.ivt.apitestgenplugin.configGen.ConfigGenerationManager;
import ru.itmo.ivt.apitestgenplugin.dataGen.DataGenerationManager;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.UserInput;
import ru.itmo.ivt.apitestgenplugin.modelGen.ParserManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.getSrcDirectory;

public class StartPluginAction extends AnAction {
    private final ParserManager parserManager = new ParserManager();
    private final TestGenerationManager testGenerationManager = new TestGenerationManager();
    private final DataGenerationManager dataGenerationManager = new DataGenerationManager();
    private final ConfigGenerationManager configGenerationManager = new ConfigGenerationManager();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        InputDialog dialog = new InputDialog(project);
        dialog.show();
        ApplicationManager.getApplication().runWriteAction(() -> {
            if (dialog.isOK()) {
                UserInput userInput = dialog.getUserInput();
                GenerationContext context = new GenerationContext();
                context.setProject(project);
                context.setUserInput(userInput);
                PsiDirectory srcDir = getSrcDirectory(project);

                parserManager.prepareGeneratorContext(userInput.openApiPath(), srcDir.getVirtualFile().getCanonicalPath(), context);
                CompletableFuture<Void> configFuture = CompletableFuture.runAsync(() ->
                        configGenerationManager.fillConfigFiles(srcDir, context));

                CompletableFuture<Void> dataFuture = CompletableFuture.runAsync(() -> {
                        dataGenerationManager.generateDataFiles(srcDir, context);
                        testGenerationManager.generateClientsAndTests(srcDir, context);
                });

                try {
                    CompletableFuture.allOf(configFuture, dataFuture).get();
                } catch (InterruptedException | ExecutionException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Error during parallel execution", ex);
                }
            }
        });
    }
}
