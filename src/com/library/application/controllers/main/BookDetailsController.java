package com.library.application.controllers.main;

import com.javafx.custom.components.AutocompleteTextInputControl;
import com.library.Main;
import com.library.application.controllers.IssueBookController;
import com.library.application.controllers.abstractions.AbstractDetailsController;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.application.scene.loaders.elements.IssueBookScene;
import com.library.application.scene.loaders.elements.ReturnBookScene;
import com.library.cache.ControllersCache;
import com.library.data.model.Book;
import com.library.i18n.I18nProvider;
import com.library.worker.admin.AdminActionsLogger;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import com.library.utils.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.ResourceBundle;

public final class BookDetailsController extends AbstractDetailsController<Book> {
    public static final String availableColor = "-fx-background-color: rgba(0, 141, 0, 0.5);";
    public static final String unavailableColor = "-fx-background-color: rgba(255, 0, 0, 0.5);";

    @FXML
    private TextField id;
    @FXML
    private TextField serial;
    @FXML
    private TextField title;
    @FXML
    private TextField author;
    @FXML
    private TextField category;
    @FXML
    private TextArea description;
    @FXML
    private TextArea notes;
    @FXML
    private TextField publishHouse;
    @FXML
    private TextField publishYear;
    @FXML
    private TextField publishCity;
    @FXML
    private TextField pages;
    @FXML
    private TextField language;
    @FXML
    private TextField condition;
    @FXML
    private TextField created;
    @FXML
    private TextField lastUpdated;
    @FXML
    private TextField available;
    @FXML
    public Button bookKeeperButton;
    @FXML
    public Button issueButton;
    @FXML
    public Button deleteBookButton;

    public static BookDetailsController getInstance() {
        return ControllersCache.bookDetailsController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        if (ControllersCache.bookDetailsController == null) {
            ControllersCache.bookDetailsController = this;
        }

        filterChoiceBox.getItems().add(new PairKey<>(resourceBundle.getString("scene.bookDetails.serialSh"), "serial"));

        setTextInputControlLimit(serial, 20);
        setTextInputControlLimit(pages, pagesDigitsSize);
        setTextInputControlLimit(publishYear, yearDigitsSize);

        setTextInputIntegerOnly(publishYear);
        setTextInputIntegerOnly(pages);

        categoryAutoComplete(category);
        languageAutoComplete(language);
        publishHouseAutoComplete(publishHouse);
    }

    public static void categoryAutoComplete(TextField category) {
        AutocompleteTextInputControl autocompleteTextInputControl = new AutocompleteTextInputControl(category, Main.getLibraryConfig().getConfiguration().getCategories());
        category.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                autocompleteTextInputControl.getEntries().addAll(Main.getLibraryConfig().getConfiguration().getCategories());
            }
        });
    }

    public static void languageAutoComplete(TextField language) {
        AutocompleteTextInputControl autocompleteTextInputControl = new AutocompleteTextInputControl(language, Main.getLibraryConfig().getConfiguration().getLanguages());
        language.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                autocompleteTextInputControl.getEntries().addAll(Main.getLibraryConfig().getConfiguration().getLanguages());
            }
        });
    }

    public static void publishHouseAutoComplete(TextField publishHouse) {
        AutocompleteTextInputControl autocompleteTextInputControl = new AutocompleteTextInputControl(publishHouse, Main.getLibraryConfig().getConfiguration().getPublishHouses());
        publishHouse.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                autocompleteTextInputControl.getEntries().addAll(Main.getLibraryConfig().getConfiguration().getPublishHouses());
            }
        });
    }

    @FXML
    protected void filterButtonAction() throws SQLException {
        ArrayList<Book> bookArrayList = CommonUtils.getFilteredBooks(filterTextField, filterChoiceBox);

        if (bookArrayList.size() > 0) {
            setFieldsData(bookArrayList.get(0));
        }
    }

    @FXML
    protected void clearButtonAction() {
        super.clearButtonAction();
        issueButton.setText(resourceBundle.getString(IssueButtonStatements.ANY.getStatement()));
    }

    protected void disableAllButtons() {
        available.setStyle(null);
        issueButton.setDisable(true);
        bookKeeperButton.setDisable(true);
        editButton.setDisable(true);
        deleteBookButton.setDisable(true);
    }

    @FXML
    protected void saveButtonAction() {
        String serialText = StringUtils.trimIfNotNull(serial.getText());
        String titleText = StringUtils.trimIfNotNull(title.getText());
        String authorText = StringUtils.trimIfNotNull(author.getText());
        String descriptionText = StringUtils.trimIfNotNull(description.getText());
        String categoryText = StringUtils.trimIfNotNull(category.getText());
        String notesText = StringUtils.trimIfNotNull(notes.getText());
        String publishHouseText = StringUtils.trimIfNotNull(publishHouse.getText());
        String publishYearText = StringUtils.trimIfNotNull(publishYear.getText());
        String publishCityText = StringUtils.trimIfNotNull(publishCity.getText());
        String pagesText = StringUtils.trimIfNotNull(pages.getText());
        String languageText = StringUtils.trimIfNotNull(language.getText());
        String conditionText = StringUtils.trimIfNotNull(condition.getText());

        if (!verifyBookInputData(serialText, titleText, authorText, categoryText, descriptionText,
                notesText, publishHouseText, publishYearText, publishCityText, pagesText, languageText))
            return;

        long timestamp = Instant.now().getEpochSecond();

        try {
            QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.add("UPDATE books SET");
            queryBuilder.addIfExists("serial", serialText);
            queryBuilder.addIfExists("title", titleText);
            queryBuilder.addIfExists("author", authorText);
            queryBuilder.addIfExists("description", descriptionText);
            queryBuilder.addIfExists("category", categoryText);
            queryBuilder.addIfExists("notes", notesText);
            queryBuilder.addIfExists("publish_house", publishHouseText);
            queryBuilder.addIfExists("publish_year", publishYearText);
            queryBuilder.addIfExists("publish_city", publishCityText);
            queryBuilder.addIfExists("pages", pagesText);
            queryBuilder.addIfExists("language", languageText);
            queryBuilder.addIfExists("last_updated", timestamp);
            queryBuilder.addIfExists("condition", conditionText);
            queryBuilder.setEnd();
            queryBuilder.add("WHERE id='" + objectMemory.getId() + "'");

            databaseHandler.executeUpdate(queryBuilder.getQuery());

            Integer pages = null;
            try {
                pages = Integer.parseInt(pagesText);
            } catch (NumberFormatException ignored) {
            }

            Book book = new Book(objectMemory.getId(), serialText, titleText, authorText,
                    descriptionText, categoryText, languageText, publishYearText,
                    publishHouseText, publishCityText, pages,
                    objectMemory.getMemberID(), objectMemory.getRentDeadline(), objectMemory.getRentNotes(), conditionText, notesText,
                    objectMemory.getCreated(), timestamp);

            setNewMemoryObject(book);

            new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("alert.bookUpdated"), id.getText()), AlertLabelType.SUCCESS);

            AdminActionsLogger.newLog(LogType.UPDATE, LogTarget.BOOK, MessageFormat.format(resourceBundle.getString("log.updateBook"), id.getText()));

            BookSearchController.getInstance().overwriteItem(book);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    @FXML
    private void bookKeeperButtonAction() throws SQLException {
        if (objectMemory.getMemberID() != null) {
            ControllerUtils.redirectToMemberDetails(getBookHandler(objectMemory));
        }
    }

    private String getSelectedFilterKey() {
        return filterChoiceBox.getSelectionModel().getSelectedItem().getKey();
    }

    public void setFilterType(BookSearchFilter BookSearchFilter) {
        filterChoiceBox.getSelectionModel().select(BookSearchFilter.value);
    }

    @FXML
    private void openIssueBookWindow() throws IOException, SQLException {
        if (objectMemory.getAvailable()) {
            IssueBookScene issueBookScene = IssueBookScene.load();

            IssueBookController issueBookController = issueBookScene.getFxmlLoader().getController();
            issueBookController.setBookData(objectMemory);

            issueBookScene.getStage().show();
        } else {
            ReturnBookScene returnBookScene = ReturnBookScene.loadWithData(getBookHandler(objectMemory), objectMemory);
            returnBookScene.getStage().show();
        }
    }

    @FXML
    private void deleteBook() throws SQLException {
        if (objectMemory.getMemberID() != null) {
            JOptionPane.showMessageDialog(null, MessageFormat.format(resourceBundle.getString("controller.bookDetails.bookIsIssued"), objectMemory.getTitle(), objectMemory.getSerial()), null, JOptionPane.ERROR_MESSAGE);
            return;
        }

        int jOptionPane = JOptionPane.showConfirmDialog(null, MessageFormat.format(resourceBundle.getString("controller.bookDetails.confirmAsk"), objectMemory.getTitle(), objectMemory.getSerial()), null, JOptionPane.YES_NO_OPTION);
        switch (jOptionPane) {
            case JOptionPane.NO_OPTION -> {
            }
            case JOptionPane.OK_OPTION -> {
                boolean deleted = databaseHandler.executeUpdate("DELETE FROM books WHERE id='" + objectMemory.getId() + "'");
                if (deleted) {
                    new AlertLabel(alertLabel, MessageFormat.format(resourceBundle.getString("alert.bookDeleted"), objectMemory.getSerial()), AlertLabelType.SUCCESS);
                    clearAllFields();
                    BookSearchController.getInstance().removeItem(objectMemory);
                    objectMemory = null;
                }
            }
        }
    }

    protected void clearAllFields() {
        filterTextField.clear();
        id.clear();
        serial.clear();
        title.clear();
        author.clear();
        category.clear();
        description.clear();
        notes.clear();
        publishHouse.clear();
        publishYear.clear();
        publishCity.clear();
        pages.clear();
        language.clear();
        condition.clear();
        created.clear();
        lastUpdated.clear();
        available.setStyle(null);
    }

    public void setFieldsData(Book book) {
        super.setFieldsData(book);
        id.setText(String.valueOf(book.getId()));
        serial.setText(book.getSerial());
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        category.setText(book.getCategory());
        description.setText(book.getDescription());
        notes.setText(book.getNotes());
        publishHouse.setText(book.getPublishHouse());
        publishYear.setText(book.getPublishYear());
        publishCity.setText(book.getPublishCity());
        pages.setText(String.valueOf(book.getPages()));
        language.setText(book.getLanguage());
        condition.setText(book.getCondition());

        if (book.getCreated() != null && book.getCreated() > 0) {
            created.setText(DateTimeUtils.getCurrentFormattedDate(book.getCreated()));
        }
        if (book.getLastUpdated() != null && book.getLastUpdated() > 0) {
            lastUpdated.setText(DateTimeUtils.getCurrentFormattedDate(book.getLastUpdated()));
        }

        setAvailable(book);
    }

    private void setAvailable(Book book) {
        boolean isAvailable = book.getAvailable();
        if (isAvailable) {
            available.setStyle(availableColor);
            issueButton.setText(resourceBundle.getString(IssueButtonStatements.ISSUE.getStatement()));
        } else {
            available.setStyle(unavailableColor);
            bookKeeperButton.setDisable(false);
            issueButton.setText(resourceBundle.getString(IssueButtonStatements.RETURN.getStatement()));
        }

        if (!isEditMode()) {
            issueButton.setDisable(false);
            deleteBookButton.setDisable(false);
        }
    }

    protected void editModeAvailableFieldsToggle(boolean toggle) {
        super.editModeAvailableFieldsToggle(toggle);
        id.setDisable(toggle);

        serial.setEditable(toggle);
        title.setEditable(toggle);
        author.setEditable(toggle);
        category.setEditable(toggle);
        description.setEditable(toggle);
        notes.setEditable(toggle);
        publishHouse.setEditable(toggle);
        publishYear.setEditable(toggle);
        publishCity.setEditable(toggle);
        pages.setEditable(toggle);
        language.setEditable(toggle);
        condition.setEditable(toggle);

        issueButton.setDisable(toggle);
        created.setDisable(toggle);
        lastUpdated.setDisable(toggle);
        available.setDisable(toggle);
        deleteBookButton.setDisable(toggle);
        bookKeeperButton.setDisable(toggle);
    }

    public static BookSearchFilter getFilterType(int index) {
        return BookSearchFilter.values()[index];
    }

    public enum BookSearchFilter {
        EMPTY(0),
        ID(1),
        SERIAL(2),
        TITLE(3);

        private final int value;

        BookSearchFilter(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum IssueButtonStatements {
        ANY("scene.bookDetails.issueReturn"),
        ISSUE("scene.issueBook.issueButton"),
        RETURN("scene.returnBook.returnButton");

        private final String statement;

        IssueButtonStatements(final String newStatement) {
            statement = newStatement;
        }

        public String getStatement() {
            return statement;
        }
    }
}
