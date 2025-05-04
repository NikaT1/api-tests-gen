package ru.itmo.ivt.apitestgenplugin.configGen;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import lombok.RequiredArgsConstructor;
import org.jetbrains.idea.maven.dom.model.*;

@RequiredArgsConstructor
public class MavenDependencyGenerator {
    private final Project project;
    private final MavenDomProjectModel model;

    public void addRestAssuredDependency() {
        addProperty("io.rest.assured.version", "5.3.2");
        addMavenDependency("io.rest-assured", "rest-assured", "${io.rest.assured.version}");
    }

    public void addJacksonDependency() {
        addProperty("jackson.databind.version", "2.16.1");
        addMavenDependency("com.fasterxml.jackson.core", "jackson-databind", "${jackson.databind.version}");
    }

    public void addJunit5Dependency() {
        addProperty("junit.jupiter.version", "5.9.0");
        addBomDependency("org.junit", "junit-bom", "${junit.jupiter.version}");
        addMavenDependency("org.junit.platform", "junit-platform-suite");
        addMavenDependency("org.junit.jupiter", "junit-jupiter-engine");
    }

    public void addLombokDependency() {
        addProperty("projectlombok.version", "1.18.30");
        addMavenDependency("org.projectlombok", "lombok", "${projectlombok.version}", "provided");
    }

    public void addJavaFakerDependency() {
        addProperty("java.faker.version", "0.2.5");
        addMavenDependency("io.github.regychang", "java-faker", "${java.faker.version}");
    }

    public void addAllureDependencyAndPlugins() {
        addProperty("maven.compiler.plugin.version", "3.10.1");
        addProperty("maven.surefire.plugin.version", "3.0.0-M7");
        addProperty("allure.version", "2.27.0");
        addProperty("aspectj.version", "1.9.20.1");
        addProperty("allure.maven.version", "2.12.0");
        addBomDependency("io.qameta.allure", "allure-bom", "${allure.version}");
        addMavenDependency("io.qameta.allure", "allure-junit-platform");
        addBuildPlugins();
    }

    private void addMavenDependency(String group, String artifact) {
        addMavenDependency(group, artifact, null, null);
    }

    private void addMavenDependency(String group, String artifact, String version) {
        addMavenDependency(group, artifact, version, null);
    }

    private void addMavenDependency(String group, String artifact, String version, String scope) {
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

    private void addProperty(String name, String value) {
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

    private void addBomDependency(String group, String artifact, String version) {
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

    private void addBuildPlugins() {
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

    private void addConfigurationProperty(MavenDomConfiguration configuration, String name, String value) {
        XmlTag configTag = configuration.getXmlTag();
        if (configTag != null) {
            XmlTag propertyTag = configTag.createChildTag(name, configTag.getNamespace(), value, false);
            configTag.add(propertyTag);
        }
    }

    private void addExecutionPhase(MavenDomPluginExecution execution, String phase) {
        execution.getPhase().setStringValue(phase);
    }

    private void addExecutionGoal(MavenDomPluginExecution execution, String goal) {
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
