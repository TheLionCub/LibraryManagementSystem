package com.library.application.scene.loaders.elements;

import com.library.application.scene.loaders.WindowManager;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class DebtorsScene extends WindowManager {
    private static final URL url = IssueBookScene.class.getClassLoader().getResource("resources/fxml/debtors.fxml");

    private DebtorsScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.debtorsList"), true, null);
    }

    public static DebtorsScene load() throws IOException {
        return new DebtorsScene(url);
    }
}
