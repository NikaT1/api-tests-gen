package ru.itmo.ivt.apitestgenplugin.dataGen;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

import java.util.Map;

public class DataGenerationManager {
    public void fillClientsAndTests(PsiDirectory srcDir, GenerationContext context) {
        Map<String, PsiFile> clientFiles = context.getClientFiles();

    }
}
