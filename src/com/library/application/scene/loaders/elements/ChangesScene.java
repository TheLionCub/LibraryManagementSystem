package com.library.application.scene.loaders.elements;

import com.library.application.scene.loaders.WindowManager;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class ChangesScene extends WindowManager {
    private static final URL url = LoginScene.class.getClassLoader().getResource("resources/fxml/settings/changes.fxml");

    private ChangesScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.changes"), true, null);
    }

    public static ChangesScene load() throws IOException {
        return new ChangesScene(url);
    }
}
