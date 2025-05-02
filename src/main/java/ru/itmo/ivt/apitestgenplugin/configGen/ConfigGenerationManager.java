package ru.itmo.ivt.apitestgenplugin.configGen;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.UserInput;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createSubDirectory;

public class ConfigGenerationManager {
    private static final String DEFAULT_MAVEN_FILE_NAME = "pom.xml";
    private static final String DEFAULT_MANAGER_PACKAGE = "manager";
    private static final String DEFAULT_CONFIG_PACKAGE = "config";

    public void fillConfigFiles(@NotNull PsiDirectory srcDir, @NotNull GenerationContext context) {
        assert context.getProject() != null;

        Project project = context.getProject();
        prepareMavenFile(project, srcDir);
        prepareUserConfigsFile(project, srcDir, context.getUserInput());
    }

    private void prepareUserConfigsFile(Project project, PsiDirectory srcDir, UserInput userInput) {
        PsiDirectory mainDir = createSubDirectory(project, srcDir, "main");
        PsiDirectory resourcesDir = createSubDirectory(project, mainDir, "resources");
        PsiDirectory javaDir = createSubDirectory(project, mainDir, "java");
        PsiDirectory managerDir = createSubDirectory(project, javaDir, DEFAULT_MANAGER_PACKAGE);
        PsiDirectory configDir = createSubDirectory(project, javaDir, DEFAULT_CONFIG_PACKAGE);

        UserConfigFileGenerator.createUserConfigFile(project, resourcesDir, userInput.baseUrl(), userInput.authType());
        UserConfigFileGenerator.createPropertiesManagerFile(project, managerDir, DEFAULT_MANAGER_PACKAGE);
        UserConfigFileGenerator.createRestAssuredConfigFile(project, configDir, DEFAULT_CONFIG_PACKAGE, userInput.authType());
    }

    private void prepareMavenFile(Project project, PsiDirectory srcDir) {
        XmlFile mavenFile = getMavenFile(srcDir, project);
        checkMavenFile(project, mavenFile);

        MavenDomProjectModel model = MavenDomUtil.getMavenDomModel(mavenFile, MavenDomProjectModel.class);
        if (model == null) {
            return;
        }

        MavenDependencyGenerator mavenDependencyGenerator = new MavenDependencyGenerator(project, model);
        mavenDependencyGenerator.addRestAssuredDependency();
        mavenDependencyGenerator.addJacksonDependency();
        mavenDependencyGenerator.addJunit5Dependency();
        mavenDependencyGenerator.addLombokDependency();
        mavenDependencyGenerator.addAllureDependencyAndPlugins();
    }

    private XmlFile getMavenFile(PsiDirectory srcDir, Project project) {
        PsiDirectory projectPsiDir = srcDir.getParentDirectory();
        assert projectPsiDir != null;

        VirtualFile projectDir = projectPsiDir.getVirtualFile();

        VirtualFile pomFile = projectDir.findChild(DEFAULT_MAVEN_FILE_NAME);
        assert pomFile != null;

        PsiFile psiFile = PsiManager.getInstance(project).findFile(pomFile);
        assert psiFile instanceof XmlFile;
        return (XmlFile) psiFile;
    }

    private void checkMavenFile(Project project, XmlFile pomFile) {
        MavenProjectsManager mavenManager = MavenProjectsManager.getInstance(project);
        MavenProject mavenProject = mavenManager.findProject(pomFile.getVirtualFile());
        assert mavenProject != null;
    }
}
