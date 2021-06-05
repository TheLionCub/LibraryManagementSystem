package com.library.application.scene.loaders.elements;

import com.library.application.scene.loaders.WindowManager;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class MemberDetailsScene extends WindowManager {
    private static final URL url = LoginScene.class.getClassLoader().getResource("resources/fxml/main/memberDetails.fxml");

    private MemberDetailsScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.memberDetails"), true, null);
    }

    public static MemberDetailsScene load() throws IOException {
        return new MemberDetailsScene(url);
    }
}
