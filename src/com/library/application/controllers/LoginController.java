package com.library.application.controllers;

import com.library.application.controllers.abstractions.AbstractController;
import com.library.application.scene.loaders.SceneManager;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.application.scene.loaders.elements.CreateAdminScene;
import com.library.application.scene.loaders.elements.LoadingLock;
import com.library.cache.SessionCache;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import com.library.utils.CommonUtils;
import com.library.utils.HashUtils;
import com.library.utils.StringUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public final class LoginController extends AbstractController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Hyperlink createUserHyperButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (CommonUtils.getAdminAccountsCount() == 0) {
            createUserHyperButton.setVisible(true);
        }
    }

    @FXML
    private void handleLoginButtonAction() throws Exception {
        if (alertLabel.isVisible()) {
            alertLabel.setVisible(false);
        }

        LoadingLock loadingLock = new LoadingLock(anchorPane);
        loadingLock.enable();

        boolean submitted = submitLogin();
        if (!submitted) {
            loadingLock.disable();
        }
    }

    public boolean submitLogin() throws Exception {
        if (usernameField.getText() == null
                || usernameField.getText().isEmpty()
                || passwordField.getText() == null
                || passwordField.getText().isEmpty()) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.login.noData"), AlertLabelType.ERROR);
            return false;
        }

        String usernameValue = StringUtils.trimIfNotNull(usernameField.getText());
        String passwordHash = HashUtils.stringToSHA256(StringUtils.trimIfNotNull(passwordField.getText()));

        ResultSet resultSet = databaseHandler.executeQuery("SELECT * FROM admin_accounts WHERE username='" + usernameValue + "' AND password='" + passwordHash + "'");
        if (resultSet.next()) {
            String sqlPasswordHash = resultSet.getString("password");
            if (!sqlPasswordHash.equals(passwordHash)) {
                new AlertLabel(alertLabel, resourceBundle.getString("controller.login.incorrectData"), AlertLabelType.ERROR);
                return false;
            }
            alertLabel.setVisible(true);

            int userID = resultSet.getInt("id");
            String fullName = resultSet.getString("full_name");

            SessionCache.userData.put("userID", Integer.toString(userID));
            SessionCache.userData.put("username", usernameValue);
            SessionCache.userData.put("fullName", fullName);
            SessionCache.userData.put("passwordHash", passwordHash);

            SceneManager sceneManager = new SceneManager(getClass().getResource("/resources/fxml/main.fxml"), resourceBundle.getString("taskbar.title.controlPanel"), true);
            sceneManager.getStage().show();

            closeLoginStage();

            AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SYSTEM, resourceBundle.getString("log.login"));
            return true;
        } else {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.login.incorrectData"), AlertLabelType.ERROR);
            return false;
        }
    }

    private void closeLoginStage() {
        Scene scene = anchorPane.getScene();
        if (scene.getWindow().isShowing()) {
            ((Stage) scene.getWindow()).close();
        }
    }

    @FXML
    private void loadCreateAdminStage() throws IOException {
        if (AdminAccountCreateController.createdFirst) {
            createUserHyperButton.setVisible(false);
            return;
        }

        CreateAdminScene createAdminScene = CreateAdminScene.load();
        createAdminScene.getStage().showAndWait();

        if (AdminAccountCreateController.createdFirst) {
            createUserHyperButton.setVisible(false);
            alertLabel.setText(null);

            usernameField.setText(SessionCache.userData.get("username"));
            passwordField.setText(SessionCache.userData.get("password"));

            SessionCache.userData.remove("password");
        }
    }
}
