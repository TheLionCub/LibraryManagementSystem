package com.library.application.scene.loaders.elements;

import com.library.application.scene.loaders.WindowManager;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class IssueBookScene extends WindowManager {
    private static final URL url = IssueBookScene.class.getClassLoader().getResource("resources/fxml/management/issueBook.fxml");

    private IssueBookScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.issueBook"), true, null);

        stage.setWidth(800);
        stage.setHeight(1000);
    }

    public static IssueBookScene load() throws IOException {
        return new IssueBookScene(url);
    }
}