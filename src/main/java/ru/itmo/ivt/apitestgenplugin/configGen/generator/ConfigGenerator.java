package ru.itmo.ivt.apitestgenplugin.configGen.generator;

import com.intellij.psi.PsiDirectory;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

public interface ConfigGenerator {
    void prepareConfigFiles(GenerationContext context, PsiDirectory srcDir);
}
