package com.library.application.scene.loaders.elements;

import com.library.application.scene.loaders.WindowManager;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class BookDetailsScene extends WindowManager {
    private static final URL url = LoginScene.class.getClassLoader().getResource("resources/fxml/main/bookDetails.fxml");

    private BookDetailsScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.bookDetails"), true, null);
    }

    public static BookDetailsScene load() throws IOException {
        return new BookDetailsScene(url);
    }
}
