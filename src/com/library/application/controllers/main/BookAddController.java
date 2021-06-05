package com.library.application.controllers.main;

import com.library.LibraryConfig;
import com.library.Main;
import com.library.application.controllers.abstractions.AbstractController;
import com.library.application.scene.loaders.components.alertLabel.AlertLabel;
import com.library.application.scene.loaders.components.alertLabel.enums.AlertLabelType;
import com.library.data.config.ConfigurationImpl;
import com.library.data.model.Book;
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
import java.util.ResourceBundle;

public final class BookAddController extends AbstractController {
    @FXML
    private TextField serial;
    @FXML
    private TextField title;
    @FXML
    private TextField author;
    @FXML
    private ComboBox<String> category;
    @FXML
    private TextArea description;
    @FXML
    private TextArea notes;
    @FXML
    private ComboBox<String> publishHouse;
    @FXML
    private TextField publishYear;
    @FXML
    private TextField publishCity;
    @FXML
    private TextField pages;
    @FXML
    private ComboBox<String> language;
    @FXML
    private Button addButton;
    @FXML
    private CheckBox dropCheckBox;

    private int equalTitleBooks = 0;
    private String lastBookTitle = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAllTextInputControlLimit(128);
        setTextInputControlLimit(serial, 20);
        setTextInputControlLimit(pages, pagesDigitsSize);
        setTextInputControlLimit(publishYear, yearDigitsSize);

        setTextInputIntegerOnly(publishYear);
        setTextInputIntegerOnly(pages);

        LibraryConfig<ConfigurationImpl> libraryConfig = Main.getLibraryConfig();
        for (String cat : libraryConfig.getConfiguration().getCategories()) {
            category.getItems().add(cat);
        }
        for (String lang : libraryConfig.getConfiguration().getLanguages()) {
            language.getItems().add(lang);
        }
        for (String house : libraryConfig.getConfiguration().getPublishHouses()) {
            publishHouse.getItems().add(house);
        }

        BookDetailsController.categoryAutoComplete(category.getEditor());
        BookDetailsController.languageAutoComplete(language.getEditor());
        BookDetailsController.publishHouseAutoComplete(publishHouse.getEditor());
    }

    @FXML
    private void onRequiredFieldChange() {
        if (!serial.getText().isEmpty() && !title.getText().isEmpty()) {
            addButton.setDisable(false);
        } else {
            addButton.setDisable(true);
        }
    }

    @FXML
    private void createNewBook() throws SQLException {
        String serialText = StringUtils.trimIfNotNull(serial.getText());
        String titleText = StringUtils.trimIfNotNull(title.getText());
        String authorText = StringUtils.trimIfNotNull(author.getText());
        String descriptionText = StringUtils.trimIfNotNull(description.getText());
        String categoryText = StringUtils.trimIfNotNull(category.getValue());
        String notesText = StringUtils.trimIfNotNull(notes.getText());
        String publishHouseText = StringUtils.trimIfNotNull(publishHouse.getValue());
        String publishYearText = StringUtils.trimIfNotNull(publishYear.getText());
        String publishCityText = StringUtils.trimIfNotNull(publishCity.getText());
        String pagesText = StringUtils.trimIfNotNull(pages.getText());
        String languageText = StringUtils.trimIfNotNull(language.getValue());

        long createdTimestamp = Instant.now().getEpochSecond();

        if (alertLabel.isVisible()) {
            alertLabel.setVisible(false);
        }

        if (!verifyBookInputData(serialText, titleText, authorText, categoryText, descriptionText, notesText, publishHouseText,
                publishYearText, publishCityText, pagesText, languageText))
            return;

        ResultSet resultSet = databaseHandler.executeQuery("SELECT * FROM books WHERE serial='" + serialText + "'");
        if (resultSet.next()) {
            new AlertLabel(alertLabel, resourceBundle.getString("alert.bookNotExists"), AlertLabelType.ERROR);
            return;
        }

        PreparedStatement preparedStatement = databaseHandler
                .getConnection()
                .prepareStatement("INSERT INTO books (" +
                        "serial," +
                        "title," +
                        "author," +
                        "description," +
                        "category," +
                        "language," +
                        "publish_year," +
                        "publish_house," +
                        "publish_city," +
                        "pages," +
                        "notes," +
                        "created) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

        if (!serialText.isEmpty()) {
            preparedStatement.setString(1, serialText);
        }

        if (!titleText.isEmpty()) {
            preparedStatement.setString(2, titleText);
        }

        if (!authorText.isEmpty()) {
            preparedStatement.setString(3, authorText);
        }

        if (!descriptionText.isEmpty()) {
            preparedStatement.setString(4, descriptionText);
        }

        if (categoryText != null && !categoryText.isEmpty()) {
            preparedStatement.setString(5, categoryText);
        }

        if (languageText != null && !languageText.isEmpty()) {
            preparedStatement.setString(6, languageText);
        }

        if (!publishYearText.isEmpty()) {
            preparedStatement.setString(7, publishYearText);
        }

        if (publishHouseText != null && !publishHouseText.isEmpty()) {
            preparedStatement.setString(8, publishHouseText);
        }

        if (!publishCityText.isEmpty()) {
            preparedStatement.setString(9, publishCityText);
        }

        Integer pages = null;
        try {
            pages = Integer.parseInt(pagesText);
        } catch (NumberFormatException ignored) {
        }

        if (!pagesText.isEmpty()) {
            preparedStatement.setInt(10, pages);
        }

        if (!notesText.isEmpty()) {
            preparedStatement.setString(11, notesText);
        }

        preparedStatement.setString(12, String.valueOf(createdTimestamp));

        preparedStatement.execute();

        ResultSet execResult = preparedStatement.getGeneratedKeys();

        if (lastBookTitle.equals(titleText)) {
            equalTitleBooks += 1;
        } else {
            equalTitleBooks = 0;
        }

        lastBookTitle = titleText;

        if (dropCheckBox.isSelected()) {
            clearAllFields();
        }

        String successText = MessageFormat.format(resourceBundle.getString("alert.bookAddedResponse"), titleText, serialText, execResult.getInt(1));

        if (equalTitleBooks > 0) {
            successText += " " + resourceBundle.getString("alert.addedEqualName") + " " + equalTitleBooks;
        }

        new AlertLabel(alertLabel, successText, AlertLabelType.SUCCESS);

        serial.clear();

        AdminActionsLogger.newLog(LogType.CREATE, LogTarget.BOOK, MessageFormat.format(resourceBundle.getString("log.newBook"), serialText, titleText));

        BookSearchController.getInstance().addItem(new Book(
                execResult.getInt(1),
                serialText,
                titleText,
                authorText,
                descriptionText,
                categoryText,
                languageText,
                publishYearText,
                publishHouseText,
                publishCityText,
                pages,
                null,
                null,
                null,
                null,
                notesText,
                createdTimestamp,
                null
        ));
    }

    @FXML
    protected void clearAllFields() {
        serial.clear();
        title.clear();
        author.clear();
        category.getEditor().clear();
        description.clear();
        notes.clear();
        publishHouse.getEditor().clear();
        publishYear.clear();
        publishCity.clear();
        pages.clear();
        language.getEditor().clear();
        alertLabel.setText(null);
    }
}
