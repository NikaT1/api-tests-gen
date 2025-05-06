package ru.itmo.ivt.apitestgenplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;
import ru.itmo.ivt.apitestgenplugin.codeGen.TestGenerationManager;
import ru.itmo.ivt.apitestgenplugin.configGen.ConfigGenerationManager;
import ru.itmo.ivt.apitestgenplugin.dataGen.DataGenerationManager;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.UserInput;
import ru.itmo.ivt.apitestgenplugin.modelGen.ParserManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

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
        if (dialog.showAndGet()) {
            UserInput userInput = dialog.getUserInput();

            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating API Tests", true) {
                @Override
                public void run(@NotNull ProgressIndicator mainIndicator) {
                    GenerationContext context = new GenerationContext();
                    context.setProject(project);
                    context.setUserInput(userInput);

                    PsiDirectory srcDir = getSrcDirectory(project);
                    String srcPath = srcDir.getVirtualFile().getCanonicalPath();

                    mainIndicator.setText("Parsing OpenAPI specification...");
                    parserManager.prepareGeneratorContext(srcPath, context);

                    runSubTask(mainIndicator, "Generating configuration files...",
                            () -> configGenerationManager.fillConfigFiles(srcDir, context));

                    runSubTask(mainIndicator, "Generating test data...",
                            () -> dataGenerationManager.generateDataFiles(srcDir, context));

                    runSubTask(mainIndicator, "Generating API tests...",
                            () -> testGenerationManager.generateClientsAndTests(srcDir, context));
                }

                private void runSubTask(ProgressIndicator mainIndicator, String text, Runnable task) {
                    mainIndicator.setText(text);
                    ProgressManager.getInstance().executeProcessUnderProgress(task, mainIndicator);
                }

                @Override
                public void onSuccess() {
                    showNotification(project, "API Tests Generation",
                            "Tests generated successfully!", NotificationType.INFORMATION);
                }

                @Override
                public void onCancel() {
                    showNotification(project, "API Tests Generation",
                            "Generation was canceled", NotificationType.WARNING);
                }

                @Override
                public void onThrowable(@NotNull Throwable error) {
                    showNotification(project, "API Tests Generation Error",
                            "Generation failed: " + error.getMessage(), NotificationType.ERROR);
                }
            });
        }
    }

    private void showNotification(Project project, String title, String content, NotificationType type) {
        Notifications.Bus.notify(
                new com.intellij.notification.Notification(
                        "API Test Generator",
                        title,
                        content,
                        type),
                project);
    }
}