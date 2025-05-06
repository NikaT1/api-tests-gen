package ru.itmo.ivt.apitestgenplugin.configGen.generator.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.xml.XmlFile;
import lombok.RequiredArgsConstructor;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.*;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.ConfigGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

import static ru.itmo.ivt.apitestgenplugin.configGen.util.MavenFileUtil.*;

@RequiredArgsConstructor
public class MavenDependencyGenerator implements ConfigGenerator {
    @Override
    public void prepareConfigFiles(GenerationContext context, PsiDirectory srcDir) {
        Project project = context.getProject();
        XmlFile mavenFile = getMavenFile(srcDir, project);
        checkMavenFile(project, mavenFile);
        ApplicationManager.getApplication().invokeLater(() -> {
            MavenDomProjectModel model = MavenDomUtil.getMavenDomModel(mavenFile, MavenDomProjectModel.class);
            if (model == null) {
                return;
            }
            addRestAssuredDependency(project, model);
            addJacksonDependency(project, model);
            addJunit5Dependency(project, model);
            addLombokDependency(project, model);
            addJavaFakerDependency(project, model);
            addAllureDependencyAndPlugins(project, model);
        });
    }

    private void addRestAssuredDependency(Project project, MavenDomProjectModel model) {
        addProperty(project, model, "io.rest.assured.version", "5.3.2");
        addMavenDependency(project, model, "io.rest-assured", "rest-assured", "${io.rest.assured.version}");
    }

    private void addJacksonDependency(Project project, MavenDomProjectModel model) {
        addProperty(project, model, "jackson.databind.version", "2.16.1");
        addMavenDependency(project, model, "com.fasterxml.jackson.core", "jackson-databind", "${jackson.databind.version}");
    }

    private void addJunit5Dependency(Project project, MavenDomProjectModel model) {
        addProperty(project, model, "junit.jupiter.version", "5.9.0");
        addBomDependency(project, model, "org.junit", "junit-bom", "${junit.jupiter.version}");
        addMavenDependency(project, model, "org.junit.platform", "junit-platform-suite");
        addMavenDependency(project, model, "org.junit.jupiter", "junit-jupiter-engine");
    }

    private void addLombokDependency(Project project, MavenDomProjectModel model) {
        addProperty(project, model, "projectlombok.version", "1.18.30");
        addMavenDependency(project, model, "org.projectlombok", "lombok", "${projectlombok.version}", "provided");
    }

    private void addJavaFakerDependency(Project project, MavenDomProjectModel model) {
        addProperty(project, model, "java.faker.version", "0.2.5");
        addMavenDependency(project, model, "io.github.regychang", "java-faker", "${java.faker.version}");
    }

    private void addAllureDependencyAndPlugins(Project project, MavenDomProjectModel model) {
        addProperty(project, model, "maven.compiler.plugin.version", "3.10.1");
        addProperty(project, model, "maven.surefire.plugin.version", "3.0.0-M7");
        addProperty(project, model, "allure.version", "2.27.0");
        addProperty(project, model, "aspectj.version", "1.9.20.1");
        addProperty(project, model, "allure.maven.version", "2.12.0");
        addBomDependency(project, model, "io.qameta.allure", "allure-bom", "${allure.version}");
        addMavenDependency(project, model, "io.qameta.allure", "allure-junit-platform");
        addBuildPlugins(project, model);
    }

    private void addBuildPlugins(Project project, MavenDomProjectModel model) {
        MavenDomBuild build = model.getBuild();
        MavenDomPlugins plugins = build.getPlugins();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                addCompilerPlugin(plugins);
                addSurefirePlugin(plugins);
                addAllurePlugin(plugins);
            } catch (Exception ignored) {
            }
        });
    }

    private void addCompilerPlugin(MavenDomPlugins plugins) {
        MavenDomPlugin plugin = plugins.addPlugin();
        plugin.getGroupId().setStringValue("org.apache.maven.plugins");
        plugin.getArtifactId().setStringValue("maven-compiler-plugin");
        plugin.getVersion().setStringValue("${maven.compiler.plugin.version}");

        MavenDomConfiguration configuration = plugin.getConfiguration();
        addConfigurationProperty(configuration, "source", "${maven.compiler.source}");
        addConfigurationProperty(configuration, "target", "${maven.compiler.target}");
        addConfigurationProperty(configuration, "encoding", "${project.build.sourceEncoding}");
    }

    private void addSurefirePlugin(MavenDomPlugins plugins) {
        MavenDomPlugin plugin = plugins.addPlugin();
        plugin.getGroupId().setStringValue("org.apache.maven.plugins");
        plugin.getArtifactId().setStringValue("maven-surefire-plugin");
        plugin.getVersion().setStringValue("${maven.surefire.plugin.version}");

        MavenDomConfiguration configuration = plugin.getConfiguration();
        addConfigurationProperty(configuration, "testFailureIgnore", "true");
        addConfigurationProperty(configuration, "argLine",
                "-Xmx1024m " +
                        "-Dfile.encoding=${project.build.sourceEncoding} " +
                        "-Dallure.results.directory=target/allure-results " +
                        "-javaagent:\"${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar\"");

        MavenDomDependencies pluginDependencies = plugin.getDependencies();
        MavenDomDependency dependency = pluginDependencies.addDependency();
        dependency.getGroupId().setStringValue("org.aspectj");
        dependency.getArtifactId().setStringValue("aspectjweaver");
        dependency.getVersion().setStringValue("${aspectj.version}");
    }

    private void addAllurePlugin(MavenDomPlugins plugins) {
        MavenDomPlugin plugin = plugins.addPlugin();
        plugin.getGroupId().setStringValue("io.qameta.allure");
        plugin.getArtifactId().setStringValue("allure-maven");
        plugin.getVersion().setStringValue("${allure.maven.version}");

        MavenDomConfiguration configuration = plugin.getConfiguration();
        addConfigurationProperty(configuration, "reportVersion", "${allure.version}");
        addConfigurationProperty(configuration, "resultsDirectory", "${project.build.directory}/allure-results");
        addConfigurationProperty(configuration, "reportDirectory", "${project.build.directory}/allure-reports");

        MavenDomExecutions executions = plugin.getExecutions();
        MavenDomPluginExecution execution = executions.addExecution();
        addExecutionPhase(execution, "install");
        addExecutionGoal(execution, "report");
    }
}
