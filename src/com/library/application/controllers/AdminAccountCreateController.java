package com.library.application.controllers;

import com.javafx.custom.components.LibTooltip;
import com.library.application.controllers.abstractions.AbstractController;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.cache.SessionCache;
import com.library.managers.windows.TrayManager;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import com.library.utils.CommonUtils;
import com.library.utils.HashUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class AdminAccountCreateController extends AbstractController {
    public static final int maxAdminAccounts = 10;

    private static final int passwordMinLength = 8;
    public static final int maxTextLength = 64;

    public static boolean createdFirst = false;

    @FXML
    private Label loginLabel;
    @FXML
    private TextField fullName;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField passwordRepeatField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showTooltips();

        anchorPane.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                try {
                    createAccount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        setAllTextInputControlLimit(128);
    }

    @FXML
    private void createAccount() throws Exception {
        if (alertLabel.isVisible()) {
            alertLabel.setVisible(false);
        }

        if (CommonUtils.getAdminAccountsCount() >= maxAdminAccounts) {
            JOptionPane.showMessageDialog(null, MessageFormat.format(resourceBundle.getString("controller.adminAccountCreate.maxAccountsLimit"), maxAdminAccounts), null, JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fullNameValue = fullName.getText();
        String loginValue = loginField.getText();
        String passwordValue = passwordField.getText();
        String passwordRepeatValue = passwordRepeatField.getText();

        if (fullNameValue.isEmpty()
                || loginValue.isEmpty()
                || passwordValue.isEmpty()
                || passwordRepeatValue.isEmpty()) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.adminAccountCreate.allFieldsRequired"), AlertLabelType.ERROR);
            return;
        }

        if (!fullNameValue.matches(CommonUtils.latinCyrillicMatcher)) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.adminAccountCreate.latinCyrillicOnly"), AlertLabelType.ERROR);
            return;
        }

        if (!loginValue.matches(CommonUtils.loginMatcher)) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.adminAccountCreate.loginMatcherOnly"), AlertLabelType.ERROR);
            return;
        }

        if (!passwordValue.equals(passwordRepeatValue)) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.adminAccountCreate.passwordsNotEqual"), AlertLabelType.ERROR);
            return;
        }

        if (!passwordValue.matches(CommonUtils.loginMatcher)) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.adminAccountCreate.passwordMatcherOnly"), AlertLabelType.ERROR);
            return;
        }

        if (loginValue.length() > maxTextLength) {
            new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("controller.adminAccountCreate.maxLoginLength"), maxTextLength), AlertLabelType.ERROR);
            return;
        }

        if (passwordValue.length() < passwordMinLength) {
            new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("controller.adminAccountCreate.minPasswordLength"), passwordMinLength), AlertLabelType.ERROR);
            return;
        }

        if (loginValue.equals(passwordValue)) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.adminAccountCreate.loginEqualsPassword"), AlertLabelType.ERROR);
            return;
        }

        ResultSet resultSet = databaseHandler.executeQuery("SELECT * FROM admin_accounts WHERE username = '" + loginValue + "'");
        if (resultSet.next()) {
            new AlertLabel(alertLabel, resourceBundle.getString("controller.adminAccountCreate.accountExists"), AlertLabelType.ERROR);
            return;
        }

        String passwordHash = HashUtils.stringToSHA256(passwordValue);

        boolean executed = databaseHandler.executeUpdate("INSERT INTO admin_accounts (username, full_name, password) VALUES" +
                "('" + loginValue + "', '" + fullNameValue + "', '" + passwordHash + "')");

        if (executed) {
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.close();

            TrayManager.sendNotification(resourceBundle.getString("controller.adminAccountCreate.accountCreated"), TrayIcon.MessageType.NONE);

            if (!createdFirst) {
                createdFirst = true;
                if (!SessionCache.userAuthorized()) {
                    SessionCache.userData.put("username", loginValue);
                    SessionCache.userData.put("password", passwordValue);
                }
            }

            AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SYSTEM, resourceBundle.getString("log.newAdmin"));
        }
    }

    private void showTooltips() {
        new LibTooltip(loginLabel, resourceBundle.getString("controller.adminAccountCreate.tooltip.loginTooltip"));
    }
}
