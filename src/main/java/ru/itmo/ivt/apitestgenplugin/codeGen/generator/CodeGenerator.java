package ru.itmo.ivt.apitestgenplugin.codeGen.generator;

import com.intellij.psi.PsiDirectory;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

public interface CodeGenerator {
    void generateCode(PsiDirectory directory, GenerationContext context);
}
