package com.library.application.scene.loaders;

import com.library.LibraryBaseAbstract;
import com.library.managers.windows.CHMManager;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class SceneManager extends LibraryBaseAbstract {
    protected final FXMLLoader fxmlLoader;
    protected final Stage stage;
    protected final Scene scene;

    public SceneManager(URL url, String title, boolean resizeable) throws IOException {
        super();

        fxmlLoader = new FXMLLoader(url, resourceBundle);

        Parent parent = fxmlLoader.load();

        stage = new Stage();
        scene = new Scene(parent);

        scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toString());

        stage.setScene(scene);
        stage.setResizable(resizeable);
        stage.setTitle(title);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/book.png")));
        stage.requestFocus();

        stage.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.F1) {
                    CHMManager.loadStart();
                }
            }
        });
    }

    public void windowScreenCenter() {
        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        int x = (int) (displayMode.getWidth() / 2 - scene.getWindow().getWidth());
        int y = (int) (displayMode.getHeight() / 2 - scene.getWindow().getHeight());

        scene.getWindow().setX(x);
        scene.getWindow().setY(y);
    }

    public FXMLLoader getFxmlLoader() {
        return fxmlLoader;
    }

    public Stage getStage() {
        return stage;
    }

    public Scene getScene() {
        return scene;
    }
}
