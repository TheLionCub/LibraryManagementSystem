package com.library;

import com.library.application.scene.loaders.elements.LoadingScene;
import com.library.application.scene.loaders.elements.LoginScene;
import com.library.data.config.ConfigurationImpl;
import com.library.database.DatabaseHandler;
import com.library.i18n.I18nProvider;
import com.library.managers.system.FileManager;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class Main extends Application {
    private static final LibraryConfig<ConfigurationImpl> libraryConfig = new LibraryConfig<>("data/config.yml", "/resources/config/config.yml", ConfigurationImpl.class);
    private static final I18nProvider localization = I18nProvider.getInstance();

    public static void main(String[] args) {
        FileManager.createFiles();

        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        LoadingScene loadingScene = new LoadingScene();
        loadingScene.loadLoadingScene();

        new Thread(() -> {
            DatabaseHandler databaseHandler = DatabaseHandler.getInstance();

            boolean connected = databaseHandler.getConnection() != null;

            if (connected) {
                Platform.runLater(() -> {
                    try {
                        LoginScene.load().getStage().show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                JOptionPane.showMessageDialog(null, localization.getResourceBundle().getString("db.failedConnection"), null, JOptionPane.ERROR_MESSAGE);
            }

            Platform.runLater(loadingScene::stopLoading);

            Thread.currentThread().interrupt();
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SYSTEM, localization.getResourceBundle().getString("adminLogger.exit"))));
    }

    public static LibraryConfig<ConfigurationImpl> getLibraryConfig() {
        return libraryConfig;
    }
}