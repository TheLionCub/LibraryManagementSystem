package com.library.application.controllers.main;

import com.javafx.custom.components.AutocompleteTextInputControl;
import com.library.Main;
import com.library.application.controllers.abstractions.AbstractDetailsController;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.cache.ControllersCache;
import com.library.data.model.Member;
import com.library.utils.*;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.ResourceBundle;

public final class MemberDetailsController extends AbstractDetailsController<Member> {
    @FXML
    private TextField id;
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
    private TextField course;
    @FXML
    private TextField birthDate;
    @FXML
    private TextField created;
    @FXML
    private TextField lastUpdated;
    @FXML
    private Button showBooksButton;
    @FXML
    private Button deleteButton;

    public static MemberDetailsController getInstance() {
        return ControllersCache.memberDetailsController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        if (ControllersCache.memberDetailsController == null) {
            ControllersCache.memberDetailsController = this;
        }

        courseAutoComplete(course);
    }

    public static void courseAutoComplete(TextField course) {
        AutocompleteTextInputControl autocompleteTextInputControl = new AutocompleteTextInputControl(course, ConfigurationUtils.getCoursesAsList(Main.getLibraryConfig().getConfiguration().getCourses()));
        course.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                autocompleteTextInputControl.getEntries().addAll(ConfigurationUtils.getCoursesAsList(Main.getLibraryConfig().getConfiguration().getCourses()));
            }
        });
    }

    @FXML
    protected void saveButtonAction() {
        String fullNameText = StringUtils.trimIfNotNull(fullName.getText());
        String emailText = StringUtils.trimIfNotNull(email.getText());
        String phoneText = StringUtils.trimIfNotNull(phone.getText());
        String addressText = StringUtils.trimIfNotNull(address.getText());
        String notesText = StringUtils.trimIfNotNull(notes.getText());
        String courseText = StringUtils.trimIfNotNull(course.getText());
        String birthDateText = StringUtils.trimIfNotNull(birthDate.getText());

        if (!verifyMemberInputData(fullNameText))
            return;

        long timestamp = Instant.now().getEpochSecond();

        try {
            QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.add("UPDATE members SET");
            queryBuilder.addIfExists("full_name", fullNameText);
            queryBuilder.addIfExists("email", emailText);
            queryBuilder.addIfExists("phone", phoneText);
            queryBuilder.addIfExists("address", addressText);
            queryBuilder.addIfExists("notes", notesText);
            queryBuilder.addIfExists("course", courseText);
            queryBuilder.addIfExists("birth_date", birthDateText);
            queryBuilder.addIfExists("last_updated", timestamp);
            queryBuilder.setEnd();
            queryBuilder.add("WHERE id='" + objectMemory.getId() + "'");

            databaseHandler.executeUpdate(queryBuilder.getQuery());

            Member member = new Member(objectMemory.getId(), fullNameText, emailText, phoneText,
                    courseText, addressText, notesText, birthDateText, objectMemory.getCreated(), timestamp);

            setNewMemoryObject(member);

            new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("alert.memberUpdated"), member.getId()), AlertLabelType.SUCCESS);

            AdminActionsLogger.newLog(LogType.UPDATE, LogTarget.MEMBER, MessageFormat.format(resourceBundle.getString("log.updateMember"), member.getId()));

            MemberSearchController.getInstance().overwriteItem(member);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void setFieldsData(Member member) {
        super.setFieldsData(member);
        id.setText(String.valueOf(member.getId()));
        fullName.setText(member.getFullName());
        email.setText(member.getEmail());
        phone.setText(member.getPhone());
        course.setText(member.getCourse());
        address.setText(member.getAddress());
        notes.setText(member.getNotes());
        birthDate.setText(member.getBirthDate());

        if (member.getCreated() != null && member.getCreated() > 0) {
            created.setText(DateTimeUtils.getCurrentFormattedDate(member.getCreated()));
        }
        if (member.getLastUpdated() != null && member.getLastUpdated() > 0) {
            lastUpdated.setText(DateTimeUtils.getCurrentFormattedDate(member.getLastUpdated()));
        }

        if (!isEditMode()) {
            deleteButton.setDisable(false);
            showBooksButton.setDisable(false);
        }
    }

    protected void editModeAvailableFieldsToggle(boolean toggle) {
        super.editModeAvailableFieldsToggle(toggle);
        id.setDisable(toggle);

        fullName.setEditable(toggle);
        email.setEditable(toggle);
        phone.setEditable(toggle);
        address.setEditable(toggle);
        notes.setEditable(toggle);
        course.setEditable(toggle);
        birthDate.setEditable(toggle);

        created.setDisable(toggle);
        lastUpdated.setDisable(toggle);
        showBooksButton.setDisable(toggle);
        deleteButton.setDisable(toggle);
    }

    @FXML
    private void deleteMember() throws SQLException {
        if (CommonUtils.memberHasAnyBook(objectMemory)) {
            JOptionPane.showMessageDialog(null, MessageFormat.format(resourceBundle.getString("controller.memberDetails.memberHasBooks"), objectMemory.getFullName(), objectMemory.getId()), null, JOptionPane.ERROR_MESSAGE);
            return;
        }

        int jOptionPane = JOptionPane.showConfirmDialog(null, MessageFormat.format(resourceBundle.getString("controller.memberDetails.confirmAsk"), objectMemory.getFullName(), objectMemory.getId()), null, JOptionPane.YES_NO_OPTION);
        switch (jOptionPane) {
            case JOptionPane.NO_OPTION -> {
            }
            case JOptionPane.OK_OPTION -> {
                boolean deleted = databaseHandler.executeUpdate("DELETE FROM members WHERE id='" + objectMemory.getId() + "'");
                if (deleted) {
                    new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("alert.memberDeleted"), objectMemory.getFullName()), AlertLabelType.SUCCESS);
                    clearAllFields();
                    MemberSearchController.getInstance().removeItem(objectMemory);
                    objectMemory = null;
                }
            }
        }
    }

    @FXML
    private void showBooks() {
        MemberSearchController.loadBookScene(objectMemory);
    }

    @FXML
    protected void filterButtonAction() throws SQLException {
        ArrayList<Member> memberArrayList = CommonUtils.getFilteredMembers(filterTextField, filterChoiceBox);

        if (memberArrayList.size() > 0) {
            setFieldsData(memberArrayList.get(0));
        }
    }

    protected void disableAllButtons() {
        deleteButton.setDisable(true);
        showBooksButton.setDisable(true);
        editButton.setDisable(true);
    }

    @FXML
    protected void clearAllFields() {
        id.clear();
        fullName.clear();
        email.clear();
        phone.clear();
        address.clear();
        notes.clear();
        course.clear();
        birthDate.clear();
        created.clear();
        lastUpdated.clear();
    }
}
