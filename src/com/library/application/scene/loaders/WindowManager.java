package com.library.application.scene.loaders;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;

public class WindowManager extends SceneManager {
    public WindowManager(URL url, String title, boolean resizeable, Window window) throws IOException {
        super(url, title, resizeable);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        if (window != null) {
            stage.initOwner(window);
        }

        stage.setWidth(800);
        stage.setHeight(600);

        stage.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    getStage().close();
                }
            }
        });
    }
}
