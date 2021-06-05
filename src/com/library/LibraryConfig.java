package com.library;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.library.data.config.Configuration;
import com.library.managers.system.FileManager;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class LibraryConfig<T extends Configuration> {
    private static final Yaml yaml = new Yaml(setDumperOptions());

    private T configuration;
    private final File configFile;
    private InputStream inputStream;
    private final ObjectMapper objectMapper = new YAMLMapper();
    private Map<String, Object> config;

    private final Class<T> clazz;

    private static DumperOptions setDumperOptions() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return options;
    }

    public LibraryConfig(String configFileName, String defaultConfigFilePath, Class<T> clazz) {
        configFile = FileManager.createFile(configFileName);

        this.clazz = clazz;

        try {
            if (!configFile.exists()) {
                Files.copy(getClass().getResourceAsStream(defaultConfigFilePath), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        loadData();

        try {
            config = objectMapper.readValue(configFile, new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    private void loadData() {
        try {
            inputStream = new FileInputStream(configFile);
            configuration = yaml.loadAs(inputStream, clazz);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        loadData();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public T getConfiguration() {
        return configuration;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public boolean saveConfig() {
        try {
            objectMapper.writeValue(configFile, getConfiguration());
            reloadConfig();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> getConfig() {
        return config;
    }
}
