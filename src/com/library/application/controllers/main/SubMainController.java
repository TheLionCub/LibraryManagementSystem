package com.library.application.controllers.main;

import com.library.application.controllers.abstractions.AbstractController;
import com.library.application.scene.loaders.WindowManager;
import com.library.cache.SessionCache;
import com.library.utils.DateTimeUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public final class SubMainController extends AbstractController {
    @FXML
    private Label loggedAs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loggedAs.setText(MessageFormat.format(resourceBundle.getString("controller.subMain.loggedAs"), SessionCache.userData.get("fullName"), new SimpleDateFormat(DateTimeUtils.dateTimeFormat).format(new Date())));
    }

    @FXML
    private void memberCreateWindow() throws IOException {
        WindowManager windowManager = new WindowManager(getClass().getResource("/resources/fxml/management/addMember.fxml"), resourceBundle.getString("taskbar.title.addMember"), true, null);
        windowManager.getStage().show();
    }
}
