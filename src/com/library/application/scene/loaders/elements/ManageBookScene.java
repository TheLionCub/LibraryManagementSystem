package com.library.application.scene.loaders.elements;

import com.library.application.scene.loaders.WindowManager;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class ManageBookScene extends WindowManager {
    private static final URL url = LoginScene.class.getClassLoader().getResource("resources/fxml/management/addBook.fxml");

    private ManageBookScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.manageBook"), true, null);
    }

    public static ManageBookScene load() throws IOException {
        return new ManageBookScene(url);
    }
}
