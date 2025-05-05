package ru.itmo.ivt.apitestgenplugin.configGen;

import com.intellij.psi.PsiDirectory;
import jakarta.validation.constraints.NotNull;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.ConfigGenerator;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.impl.MavenDependencyGenerator;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.impl.PropertiesConfigFileGenerator;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.impl.PropertiesManagerFileGenerator;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.impl.RestAssuredConfigFileGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

import java.util.ArrayList;
import java.util.List;

public class ConfigGenerationManager {
    private final List<ConfigGenerator> generators = new ArrayList<>();

    public void fillConfigFiles(@NotNull PsiDirectory srcDir, @NotNull GenerationContext context) {
        assert context.getProject() != null;
        prepareGenerators();
        generators.forEach((generator) -> generator.prepareConfigFiles(context, srcDir));
    }

    private void prepareGenerators() {
        generators.add(new MavenDependencyGenerator());
        generators.add(new PropertiesConfigFileGenerator());
        generators.add(new PropertiesManagerFileGenerator());
        generators.add(new RestAssuredConfigFileGenerator());
    }
}
