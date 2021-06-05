package com.library.application.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public final class LoadingController implements Initializable {
    @FXML
    private AnchorPane anchorPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void stopLoading() {
        closeLoadingWindow();
    }

    private void closeLoadingWindow() {
        Platform.runLater(() -> {
            Scene scene = anchorPane.getScene();
            if (scene.getWindow().isShowing()) {
                ((Stage) scene.getWindow()).close();
            }
        });
    }
}
