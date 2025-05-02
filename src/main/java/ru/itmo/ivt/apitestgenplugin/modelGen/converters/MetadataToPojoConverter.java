package ru.itmo.ivt.apitestgenplugin.modelGen.converters;

import java.io.File;
import java.io.IOException;

public interface MetadataToPojoConverter {
    void convertMetadataToPojo(String metadata, File outputDirectory, String outputPackage, String javaClassName) throws IOException;
}
