package com.library.application.scene.loaders.elements;

import com.library.application.scene.loaders.WindowManager;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class SettingsScene extends WindowManager {
    private static final URL url = LoginScene.class.getClassLoader().getResource("resources/fxml/settings/common.fxml");

    private SettingsScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.settings"), true, null);
    }

    public static SettingsScene load() throws IOException {
        return new SettingsScene(url);
    }
}
