package com.library.managers.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class FileManager {
    public static void createFiles() {
        copyFile("data/library.chm", "resources/docs/library.chm");
    }

    public static File createFile(String fileName) {
        createDirectoryIfNotExists(getFileDirectoryPath(fileName));

        return new File(fileName);
    }

    private static String getFileDirectoryPath(String filePath) {
        String[] paths = filePath.split("/");

        if (paths.length == 1) {
            return null;
        }

        return String.join("/", Arrays.copyOf(paths, paths.length - 1));
    }

    public static File copyFile(String fileName, String resourcePath) {
        File file = createFile(fileName);

        InputStream inputStream = FileManager.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream != null) {
            if (!file.exists()) {
                try {
                    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;
    }

    private static void createDirectoryIfNotExists(String path) {
        try {
            if (path != null) {
                Files.createDirectories(Paths.get(path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
