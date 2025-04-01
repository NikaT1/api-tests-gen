package ru.itmo.ivt.apitestgenplugin.parser.file;

import io.swagger.v3.oas.models.Paths;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface PackagesManager {
    void splitModelFilesByDirectories(Map<String, List<String>> modelsByControllers, String directory);
    void clearModelFile(File file);
    Map<String, List<String>> getModelsByControllers(Paths paths);
}
