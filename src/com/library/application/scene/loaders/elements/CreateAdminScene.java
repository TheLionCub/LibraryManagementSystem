package com.library.application.scene.loaders.elements;

import com.library.application.scene.loaders.WindowManager;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class CreateAdminScene extends WindowManager {
    private static final URL url = LoginScene.class.getClassLoader().getResource("resources/fxml/newAdminAccount.fxml");

    private CreateAdminScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.newAdmin"), false, null);

        getStage().setHeight(450);
        getStage().setWidth(320);
    }

    public static CreateAdminScene load() throws IOException {
        return new CreateAdminScene(url);
    }
}
