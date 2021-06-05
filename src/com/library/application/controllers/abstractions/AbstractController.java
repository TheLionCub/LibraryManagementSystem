package com.library.application.controllers.abstractions;

import com.library.LibraryBaseAbstract;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.application.scene.loaders.elements.*;
import com.library.cache.ControllersCache;
import com.library.cache.SessionCache;
import com.library.data.model.Book;
import com.library.data.model.Member;
import com.library.database.DatabaseHandler;
import com.library.managers.windows.CHMManager;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import com.library.utils.CommonUtils;
import com.library.utils.ControllerUtils;
import com.library.utils.DateTimeUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractController extends LibraryBaseAbstract implements Initializable {
    protected DatabaseHandler databaseHandler = DatabaseHandler.getInstance();

    @FXML
    protected Label alertLabel;
    @FXML
    protected AnchorPane anchorPane;

    protected void setAllTextInputControlLimit(final int limit) {
        ArrayList<TextInputControl> inputControls = ControllerUtils.getAllInputControls(anchorPane);

        for (TextInputControl inputControl : inputControls) {
            setTextInputControlLimit(inputControl, limit);
        }
    }

    public void setTextInputControlLimit(TextInputControl inputControl, final int limit) {
        inputControl.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (inputControl.getText() != null && inputControl.getText().length() > limit) {
                    inputControl.setText(inputControl.getText().substring(0, limit));
                }
            }
        });
    }

    public void setTextInputIntegerOnly(TextInputControl inputControl) {
        inputControl.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null && !t1.matches("\\d*")) {
                    inputControl.setText(t1.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    public LocalDate datePickerValueToLocalDate(DatePicker datePicker) {
        String text = datePicker.getEditor().getText();

        LocalDate localDate = null;

        if (text != null && !text.isEmpty()) {
            try {
                localDate = LocalDate.parse(text, DateTimeUtils.datePickerEditorFormatter);
            } catch (Exception ignored) {
            }
        }

        return localDate;
    }

    @FXML
    private void openIssueBookWindow() throws IOException {
        IssueBookScene issueBookScene = IssueBookScene.load();
        issueBookScene.getStage().show();
    }

    @FXML
    private void openReturnBookWindow() throws IOException {
        ReturnBookScene returnBookScene = ReturnBookScene.load();
        returnBookScene.getStage().show();
    }

    @FXML
    protected void openManageBookWindow() throws IOException {
        ManageBookScene manageBookScene = ManageBookScene.load();
        manageBookScene.getStage().show();
    }

    @FXML
    protected void openDebtorsWindow() throws IOException {
        DebtorsScene debtorsScene = DebtorsScene.load();
        debtorsScene.getStage().show();
    }

    @FXML
    protected void handleDocsButton() {
        CHMManager.loadStart();
    }

    protected void logout() {
        List<Window> windows = new ArrayList<>(Window.getWindows());
        for (Window window : windows) {
            if (window != null && window.isShowing()) {
                window.hide();
            }
        }

        AdminActionsLogger.newLog(LogType.CREATE, LogTarget.SYSTEM, resourceBundle.getString("adminLogger.exit"));
        ControllersCache.clearCache();
        SessionCache.userData.clear();

        try {
            LoginScene.load().getStage().show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void clearAllFields() {
    }

    protected Member getBookHandler(Book book) throws SQLException {
        if (book.getMemberID() != null) {
            ArrayList<Member> members = CommonUtils.getMembersData("SELECT * FROM members WHERE id='" + book.getMemberID() + "'");
            if (members.size() > 0) {
                return members.get(0);
            }
        }
        return null;
    }

    public static final int yearDigitsSize = 4;
    public static final int pagesDigitsSize = 5;

    protected boolean verifyBookInputData(String serial,
                                          String title,
                                          String author,
                                          String category,
                                          String description,
                                          String notes,
                                          String publishHouse,
                                          String publishYear,
                                          String publishCity,
                                          String pages,
                                          String language) {

        if (serial == null || serial.isEmpty()) {
            new AlertLabel(alertLabel, resourceBundle.getString("alert.specifySerial"), AlertLabelType.ERROR);
            return false;
        }

        if (title == null || title.isEmpty()) {
            new AlertLabel(alertLabel, resourceBundle.getString("alert.specifyTitle"), AlertLabelType.ERROR);
            return false;
        }

        if (publishYear != null && publishYear.length() > 0) {
            if (!CommonUtils.stringIsInteger(publishYear) || publishYear.length() > yearDigitsSize) {
                new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("alert.yearMaxDigits"), yearDigitsSize), AlertLabelType.ERROR);
                return false;
            }
        }

        if (pages != null && pages.length() > 0) {
            if (!CommonUtils.stringIsInteger(pages) || pages.length() > pagesDigitsSize) {
                new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("alert.pagesMaxDigits"), pagesDigitsSize), AlertLabelType.ERROR);
                return false;
            }
        }

        return true;
    }

    protected boolean verifyMemberInputData(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            new AlertLabel(alertLabel, resourceBundle.getString("alert.specifyFullName"), AlertLabelType.ERROR);
            return false;
        }

        return true;
    }
}
