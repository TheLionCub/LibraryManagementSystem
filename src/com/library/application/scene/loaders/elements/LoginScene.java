package com.library.application.scene.loaders.elements;

import com.library.application.controllers.LoginController;
import com.library.application.scene.loaders.SceneManager;
import com.library.i18n.I18nProvider;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URL;

public class LoginScene extends SceneManager {
    private static final URL url = LoginScene.class.getClassLoader().getResource("resources/fxml/login.fxml");

    private LoginScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.login"), false);

        LoginController loginController = getFxmlLoader().getController();
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    try {
                        loginController.submitLogin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static LoginScene load() throws IOException {
        return new LoginScene(url);
    }
}
