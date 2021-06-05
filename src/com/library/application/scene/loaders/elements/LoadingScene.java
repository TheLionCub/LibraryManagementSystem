package com.library.application.scene.loaders.elements;

import com.library.application.controllers.LoadingController;
import com.library.application.scene.loaders.SceneManager;
import com.library.i18n.I18nProvider;
import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoadingScene {
    private FXMLLoader fxmlLoader;

    public void loadLoadingScene() throws IOException {
        SceneManager sceneManager = new SceneManager(getClass().getResource("/resources/fxml/loading.fxml"), I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.loading"), false);
        fxmlLoader = sceneManager.getFxmlLoader();

        sceneManager.getStage().initStyle(StageStyle.TRANSPARENT);
        sceneManager.getScene().setFill(Color.TRANSPARENT);
        sceneManager.getScene().getRoot().setStyle("-fx-background-color: transparent");
        sceneManager.getStage().show();
    }

    public void stopLoading() {
        LoadingController loadingController = fxmlLoader.getController();
        loadingController.stopLoading();
    }
}
