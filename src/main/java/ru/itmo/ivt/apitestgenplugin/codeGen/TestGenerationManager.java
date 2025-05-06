package ru.itmo.ivt.apitestgenplugin.codeGen;

import com.intellij.psi.PsiDirectory;
import ru.itmo.ivt.apitestgenplugin.codeGen.generator.CodeGenerator;
import ru.itmo.ivt.apitestgenplugin.codeGen.generator.impl.ClientFilesGenerator;
import ru.itmo.ivt.apitestgenplugin.codeGen.generator.impl.TestFilesGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

import java.util.ArrayList;
import java.util.List;

public class TestGenerationManager {
    private final List<CodeGenerator> generators = new ArrayList<>();

    public void generateClientsAndTests(PsiDirectory srcDir, GenerationContext context) {
        assert context.getModelFilesByPackages() != null;
        assert context.getOpenAPI() != null;
        assert context.getOpenAPI().getPaths() != null;
        prepareGenerators();
        generators.forEach(generator -> generator.generateCode(srcDir, context));
    }

    private void prepareGenerators() {
        generators.add(new ClientFilesGenerator());
        generators.add(new TestFilesGenerator());
    }
}
