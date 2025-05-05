package ru.itmo.ivt.apitestgenplugin.configGen.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import lombok.experimental.UtilityClass;
import org.jetbrains.idea.maven.dom.model.*;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

@UtilityClass
public class MavenFileUtil {
    private static final String DEFAULT_MAVEN_FILE_NAME = "pom.xml";

    public static XmlFile getMavenFile(PsiDirectory srcDir, Project project) {
        PsiDirectory projectPsiDir = srcDir.getParentDirectory();
        assert projectPsiDir != null;

        VirtualFile projectDir = projectPsiDir.getVirtualFile();

        VirtualFile pomFile = projectDir.findChild(DEFAULT_MAVEN_FILE_NAME);
        assert pomFile != null;

        PsiFile psiFile = PsiManager.getInstance(project).findFile(pomFile);
        assert psiFile instanceof XmlFile;
        return (XmlFile) psiFile;
    }

    public static void checkMavenFile(Project project, XmlFile pomFile) {
        MavenProjectsManager mavenManager = MavenProjectsManager.getInstance(project);
        MavenProject mavenProject = mavenManager.findProject(pomFile.getVirtualFile());
        assert mavenProject != null;
    }

    public static void addMavenDependency(Project project, MavenDomProjectModel model, String group, String artifact) {
        addMavenDependency(project, model, group, artifact, null, null);
    }

    public static void addMavenDependency(Project project, MavenDomProjectModel model, String group, String artifact, String version) {
        addMavenDependency(project, model, group, artifact, version, null);
    }

    public static void addMavenDependency(Project project,
                                          MavenDomProjectModel model,
                                          String group, String artifact,
                                          String version, String scope) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                MavenDomDependency dependency = model.getDependencies().addDependency();
                dependency.getGroupId().setStringValue(group);
                dependency.getArtifactId().setStringValue(artifact);
                if (version != null) {
                    dependency.getVersion().setStringValue(version);
                }
                if (scope != null) {
                    dependency.getScope().setStringValue(scope);
                }
            } catch (Exception ignored) {
            }
        });
    }

    public static void addProperty(Project project, MavenDomProjectModel model, String name, String value) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                MavenDomProperties properties = model.getProperties();
                XmlTag propertiesTag = properties.getXmlTag();
                XmlTag propertyTag = propertiesTag.createChildTag(name, propertiesTag.getNamespace(), value, false);
                propertiesTag.add(propertyTag);
            } catch (Exception ignored) {
            }
        });
    }

    public static void addBomDependency(Project project,
                                        MavenDomProjectModel model,
                                        String group, String artifact,
                                        String version) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                MavenDomDependency dependency = model.getDependencyManagement().getDependencies().addDependency();
                dependency.getGroupId().setStringValue(group);
                dependency.getArtifactId().setStringValue(artifact);
                dependency.getVersion().setStringValue(version);
                dependency.getType().setStringValue("pom");
                dependency.getScope().setStringValue("import");
            } catch (Exception ignored) {
            }
        });
    }

    public static void addConfigurationProperty(MavenDomConfiguration configuration, String name, String value) {
        XmlTag configTag = configuration.getXmlTag();
        if (configTag != null) {
            XmlTag propertyTag = configTag.createChildTag(name, configTag.getNamespace(), value, false);
            configTag.add(propertyTag);
        }
    }

    public static void addExecutionPhase(MavenDomPluginExecution execution, String phase) {
        execution.getPhase().setStringValue(phase);
    }

    public static void addExecutionGoal(MavenDomPluginExecution execution, String goal) {
        XmlTag executionTag = execution.getXmlTag();
        if (executionTag != null) {
            XmlTag goalsTag = executionTag.findFirstSubTag("goals");
            if (goalsTag == null) {
                goalsTag = executionTag.createChildTag("goals", executionTag.getNamespace(), null, false);
                executionTag.add(goalsTag);
            }

            XmlTag goalTag = goalsTag.createChildTag("goal", goalsTag.getNamespace(), goal, false);
            goalsTag.add(goalTag);
        }
    }
}
