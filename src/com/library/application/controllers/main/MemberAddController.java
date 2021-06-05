package com.library.application.controllers.main;

import com.library.LibraryConfig;
import com.library.Main;
import com.library.application.controllers.abstractions.AbstractController;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.data.config.ConfigurationImpl;
import com.library.data.config.data.model.Course;
import com.library.data.model.Member;
import com.library.utils.ConfigurationUtils;
import com.library.utils.DateTimeUtils;
import com.library.utils.StringUtils;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

public final class MemberAddController extends AbstractController {
    @FXML
    private TextField fullName;
    @FXML
    private TextField email;
    @FXML
    private TextField phone;
    @FXML
    private TextArea address;
    @FXML
    private TextArea notes;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<String> courseChoiceBox;
    @FXML
    private Button createMemberButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        datePicker.setDayCellFactory(datePicker1 -> new DateCell() {
            @Override
            public void updateItem(LocalDate localDate, boolean empty) {
                super.updateItem(localDate, empty);
                LocalDate now = LocalDate.now();

                setDisable(empty || localDate.compareTo(now) > 0);
            }
        });

        datePicker.setValue(null);

        setAllTextInputControlLimit(128);
        setTextInputIntegerOnly(phone);

        setTextInputControlLimit(phone, 30);

        LibraryConfig<ConfigurationImpl> libraryConfig = Main.getLibraryConfig();
        ArrayList<Course> courses = libraryConfig.getConfiguration().getCourses();

        courseChoiceBox.getItems().add(null);
        for (String course : ConfigurationUtils.getCoursesAsList(courses)) {
            courseChoiceBox.getItems().add(course);
        }
    }

    @FXML
    private void onRequiredFieldChange() {
        if (fullName.getText() == null || fullName.getText().isEmpty()) {
            createMemberButton.setDisable(true);
        } else {
            createMemberButton.setDisable(false);
        }
    }

    @FXML
    private void createNewMember() throws SQLException {
        String fullNameText = StringUtils.trimIfNotNull(fullName.getText());
        String emailText = StringUtils.trimIfNotNull(email.getText());
        String phoneText = StringUtils.trimIfNotNull(phone.getText());
        String addressText = StringUtils.trimIfNotNull(address.getText());
        String notesText = StringUtils.trimIfNotNull(notes.getText());
        String courseText = courseChoiceBox.getValue();
        LocalDate birthDate = datePickerValueToLocalDate(datePicker);

        long createdTimestamp = Instant.now().getEpochSecond();

        if (alertLabel.isVisible()) {
            alertLabel.setVisible(false);
        }

        if (!verifyMemberInputData(fullNameText))
            return;

        if (birthDate != null) {
            if (birthDate.compareTo(LocalDate.now()) > 0) {
                new AlertLabel(alertLabel, resourceBundle.getString("alert.birthDateCompare"), AlertLabelType.ERROR);
                return;
            }
        }

        PreparedStatement preparedStatement = databaseHandler
                .getConnection()
                .prepareStatement("INSERT INTO members (" +
                        "full_name," +
                        "email," +
                        "phone," +
                        "course," +
                        "address," +
                        "notes," +
                        "birth_date) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

        if (!fullNameText.isEmpty()) {
            preparedStatement.setString(1, fullNameText);
        }

        if (!emailText.isEmpty()) {
            preparedStatement.setString(2, emailText);
        }

        if (!phoneText.isEmpty()) {
            preparedStatement.setString(3, phoneText);
        }

        if (courseText != null && !courseText.isEmpty()) {
            preparedStatement.setString(4, courseText);
        }

        if (!addressText.isEmpty()) {
            preparedStatement.setString(5, addressText);
        }

        if (!notesText.isEmpty()) {
            preparedStatement.setString(6, notesText);
        }

        String birthDateVal = null;

        if (birthDate != null) {
            birthDateVal = birthDate.format(DateTimeFormatter.ofPattern(DateTimeUtils.dateFormat));
            preparedStatement.setString(7, birthDateVal);
        }

        preparedStatement.execute();

        ResultSet execResult = preparedStatement.getGeneratedKeys();

        new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("alert.memberAddedResponse"), fullNameText), AlertLabelType.SUCCESS);

        AdminActionsLogger.newLog(LogType.CREATE, LogTarget.MEMBER, MessageFormat.format(resourceBundle.getString("log.newMember"), fullNameText, execResult.getInt(1)));

        MemberSearchController.getInstance().addItem(new Member(
                execResult.getInt(1),
                fullNameText,
                emailText,
                phoneText,
                courseText,
                addressText,
                notesText,
                birthDateVal,
                createdTimestamp,
                null
        ));

        clearAllFields();
    }

    @FXML
    protected void clearAllFields() {
        fullName.clear();
        email.clear();
        phone.clear();
        address.clear();
        notes.clear();
        datePicker.setValue(null);
        datePicker.getEditor().clear();
        courseChoiceBox.getSelectionModel().select(0);
    }
}
