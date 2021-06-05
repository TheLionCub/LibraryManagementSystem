package com.library.utils;

import com.library.application.controllers.main.BookDetailsController;
import com.library.application.controllers.main.MemberSearchController;
import com.library.data.model.Book;
import com.library.data.model.Change;
import com.library.data.model.Member;
import com.library.database.DatabaseHandler;
import com.library.i18n.I18nProvider;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CommonUtils {
    public static final String unicodeMatcher = "^\\p{L}+";
    public static final String loginMatcher = "^[a-zA-Z0-9@$!%*?&#^_.+]+$";
    public static final String latinCyrillicMatcher = "^[a-zA-Zа-яА-Я ]+$";

    public static ArrayList<Book> getBooksData(String sqlQuery) throws SQLException {
        ResultSet resultSet = DatabaseHandler.getInstance().executeQuery(sqlQuery);

        ArrayList<Book> bookArrayList = new ArrayList<>();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String serial = resultSet.getString("serial");
            String title = resultSet.getString("title");
            String author = resultSet.getString("author");
            String description = resultSet.getString("description");
            String category = resultSet.getString("category");
            String language = resultSet.getString("language");
            String publishYear = resultSet.getString("publish_year");
            String publishHouse = resultSet.getString("publish_house");
            String publishCity = resultSet.getString("publish_city");
            String pagesString = resultSet.getString("pages");
            String memberID = resultSet.getString("member_id");
            Long rentDeadline = resultSet.getLong("rent_deadline");
            String rentNotes = resultSet.getString("rent_notes");
            String condition = resultSet.getString("condition");
            String notes = resultSet.getString("notes");
            long created = resultSet.getLong("created");
            long lastUpdated = resultSet.getLong("last_updated");

            Integer pages = null;
            try {
                pages = Integer.parseInt(pagesString);
            } catch (NumberFormatException ignored) {
            }

            bookArrayList.add(new Book(id,
                    serial,
                    title,
                    author,
                    description,
                    category,
                    language,
                    publishYear,
                    publishHouse,
                    publishCity,
                    pages,
                    memberID,
                    rentDeadline,
                    rentNotes,
                    condition,
                    notes,
                    created,
                    lastUpdated));
        }

        return bookArrayList;
    }

    public static ArrayList<Member> getMembersData(String sqlQuery) throws SQLException {
        ResultSet resultSet = DatabaseHandler.getInstance().executeQuery(sqlQuery);

        ArrayList<Member> memberArrayList = new ArrayList<>();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String fullName = resultSet.getString("full_name");
            String email = resultSet.getString("email");
            String phone = resultSet.getString("phone");
            String course = resultSet.getString("course");
            String address = resultSet.getString("address");
            String notes = resultSet.getString("notes");
            String birthDate = resultSet.getString("birth_date");
            Long created = resultSet.getLong("created");
            Long lastUpdated = resultSet.getLong("last_updated");

            memberArrayList.add(new Member(id,
                    fullName,
                    email,
                    phone,
                    course,
                    address,
                    notes,
                    birthDate,
                    created,
                    lastUpdated));
        }

        return memberArrayList;
    }

    public static ArrayList<Change> getChangesData(String sqlQuery) throws SQLException {
        ResultSet resultSet = DatabaseHandler.getInstance().executeQuery(sqlQuery);

        ArrayList<Change> changeArrayList = new ArrayList<>();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int executorID = resultSet.getInt("executor_id");
            String username = resultSet.getString("username");
            String fullName = resultSet.getString("full_name");
            LogType logType = EnumUtils.getEnumIDValue(LogType.class, resultSet.getInt("type"));
            LogTarget logTarget = EnumUtils.getEnumIDValue(LogTarget.class, resultSet.getInt("target"));
            String details = resultSet.getString("details");
            long timestamp = resultSet.getLong("timestamp");
            String datetime = DateTimeUtils.getCurrentFormattedDate(timestamp);

            changeArrayList.add(new Change(id,
                    executorID,
                    username,
                    fullName,
                    logType,
                    logTarget,
                    details,
                    timestamp,
                    datetime));
        }

        return changeArrayList;
    }

    public static ArrayList<Book> getFilteredBooks(TextField filterTextField, ChoiceBox<PairKey<String, String>> filterChoiceBox) throws SQLException {
        ArrayList<Book> bookArrayList;

        if (BookDetailsController
                .getFilterType(filterChoiceBox.getSelectionModel().getSelectedIndex())
                .getValue() == BookDetailsController.BookSearchFilter.EMPTY.getValue()) {
            String[] keys = ControllerUtils.getNotEmptyKeys(filterChoiceBox);

            bookArrayList = getBooksData("SELECT * FROM books WHERE " +
                    DatabaseUtils.getAnyColumnQueryPart(filterTextField.getText(), keys, true));
        } else {
            bookArrayList = getBooksData("SELECT * FROM books WHERE " +
                    filterChoiceBox.getSelectionModel().getSelectedItem().getKey() + "='" + filterTextField.getText() + "'");
        }

        return bookArrayList;
    }

    public static ArrayList<Member> getFilteredMembers(TextField filterTextField, ChoiceBox<PairKey<String, String>> filterChoiceBox) throws SQLException {
        ArrayList<Member> memberArrayList;

        if (MemberSearchController
                .getFilterType(filterChoiceBox.getSelectionModel().getSelectedIndex())
                .getValue() == MemberSearchController.MemberSearchFilter.EMPTY.getValue()) {
            String[] keys = ControllerUtils.getNotEmptyKeys(filterChoiceBox);

            memberArrayList = getMembersData("SELECT * FROM members WHERE " +
                    DatabaseUtils.getAnyColumnQueryPart(filterTextField.getText(), keys, true));
        } else {
            memberArrayList = getMembersData("SELECT * FROM members WHERE " +
                    filterChoiceBox.getSelectionModel().getSelectedItem().getKey() + "='" + filterTextField.getText() + "'");
        }

        return memberArrayList;
    }

    public static ArrayList<Book> getHeldMemberBooks(Member member) throws SQLException {
        return getBooksData("SELECT * FROM books WHERE member_id='" + member.getId() + "'");
    }

    public static int getAdminAccountsCount() {
        try {
            ResultSet adminRowsCount = DatabaseHandler.getInstance().executeQuery("SELECT COUNT(*) AS rowcount FROM admin_accounts");
            adminRowsCount.next();
            return adminRowsCount.getInt("rowcount");
        } catch (SQLException sqlException) {
            return 0;
        }
    }

    public static ObservableList<PairKey<String, String>> getBookSearchFilters() {
        ObservableList<PairKey<String, String>> observableList = FXCollections.observableArrayList();
        observableList.add(BookDetailsController.BookSearchFilter.EMPTY.getValue(), new PairKey<>("", ""));
        observableList.add(BookDetailsController.BookSearchFilter.ID.getValue(), new PairKey<>("ID", "id"));
        observableList.add(BookDetailsController.BookSearchFilter.SERIAL.getValue(), new PairKey<>(I18nProvider.getLocalization().getResourceBundle().getString("scene.bookDetails.serialSh"), "serial"));
        observableList.add(BookDetailsController.BookSearchFilter.TITLE.getValue(), new PairKey<>(I18nProvider.getLocalization().getResourceBundle().getString("scene.bookDetails.title"), "title"));
        return observableList;
    }

    public static ObservableList<PairKey<String, String>> getMemberSearchFilters() {
        ObservableList<PairKey<String, String>> observableList = FXCollections.observableArrayList();
        observableList.add(MemberSearchController.MemberSearchFilter.EMPTY.getValue(), new PairKey<>("", ""));
        observableList.add(MemberSearchController.MemberSearchFilter.ID.getValue(), new PairKey<>("ID", "id"));
        observableList.add(MemberSearchController.MemberSearchFilter.FULL_NAME.getValue(), new PairKey<>(I18nProvider.getLocalization().getResourceBundle().getString("scene.memberDetails.fullName"), "full_name"));
        return observableList;
    }

    public static boolean stringIsInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public static boolean memberHasAnyBook(Member member) {
        try {
            ResultSet resultSet = DatabaseHandler.getInstance().executeQuery("SELECT * FROM books WHERE member_id='" + member.getId() + "'");
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return false;
    }
}
