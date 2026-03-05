package com.medialab.infra.json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonStorage {
    private final Path dir;
    private final ObjectMapper om;

    public JsonStorage(Path dir) {
        this.dir = dir;
         this.om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void ensureDir() {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create directory: " + dir, e);
        }
    }

    public <T> List<T> readList(String fileName, TypeReference<List<T>> typeRef) {
        Path path = dir.resolve(fileName);
        
        if (!Files.exists(path)) return List.of();

        try {
            return om.readValue(path.toFile(), typeRef);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read JSON file: " + fileName, e);
        }
    }

    public <T> void writeList(String fileName, List<T> data) {
        ensureDir();
        Path path = dir.resolve(fileName);

        try {
            om.writeValue(path.toFile(), data);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write JSON file: " + fileName, e);
        }
    }
}