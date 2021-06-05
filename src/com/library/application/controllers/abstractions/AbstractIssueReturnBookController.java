package com.library.application.controllers.abstractions;

import com.library.application.controllers.main.MemberSearchController;
import com.library.data.RowItem;
import com.library.data.model.Book;
import com.library.data.model.Member;
import com.library.utils.CommonUtils;
import com.library.utils.PairKey;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public abstract class AbstractIssueReturnBookController extends AbstractTableController<RowItem> {
    @FXML
    protected TextField filterTextFieldBook;
    @FXML
    protected ChoiceBox<PairKey<String, String>> filterChoiceBoxMember;
    @FXML
    protected TextField filterTextFieldMember;
    @FXML
    protected TableView<Book> bookTableView;
    @FXML
    protected TableView<Member> memberTableView;
    @FXML
    protected TextField selectedMemberFullName;
    @FXML
    protected DatePicker datePicker;
    @FXML
    protected TextField notes;
    @FXML
    protected Label alertLabel;

    @FXML
    protected TableColumn<Book, Integer> bookIDColumn;
    @FXML
    protected TableColumn<Book, String> bookSerialColumn;
    @FXML
    protected TableColumn<Book, String> bookTitleColumn;
    @FXML
    protected TableColumn<Book, String> bookAuthorColumn;
    @FXML
    protected TableColumn<Book, String> bookCategoryColumn;
    @FXML
    protected TableColumn<Book, String> bookLanguageColumn;
    @FXML
    protected TableColumn<Book, Integer> bookPublishYearColumn;
    @FXML
    protected TableColumn<Book, String> bookPublishHouseColumn;
    @FXML
    protected TableColumn<Book, String> bookPublishCityColumn;
    @FXML
    protected TableColumn<Book, String> bookPagesColumn;

    @FXML
    protected TableColumn<Member, Integer> memberIDColumn;
    @FXML
    protected TableColumn<Member, String> memberFullNameColumn;
    @FXML
    protected TableColumn<Member, String> memberEMailColumn;
    @FXML
    protected TableColumn<Member, String> memberPhoneColumn;
    @FXML
    protected TableColumn<Member, String> memberCourseColumn;

    protected Member selectedMember;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        filterChoiceBoxMember.setItems(CommonUtils.getMemberSearchFilters());

        filterChoiceBoxMember.getSelectionModel().select(MemberSearchController.MemberSearchFilter.FULL_NAME.getValue());

        bookTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    bookTableViewOnClicked();
                }
            }
        });

        memberTableView.setOnMouseClicked(new EventHandler<>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    selectMember(memberTableView.getSelectionModel().getSelectedItem());
                }
            }
        });

        filterTextFieldMember.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                findMembers();
            }
        });

        memberTableView.setRowFactory(memberTableView1 -> {
            TableRow<Member> memberTableRow = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();
            MenuItem selectItem = new MenuItem(resourceBundle.getString("button.select"));

            contextMenu.getItems().add(selectItem);

            selectItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectMember(memberTableView.getSelectionModel().getSelectedItem());
                }
            });

            memberTableView.contextMenuProperty().setValue(contextMenu);
            return memberTableRow;
        });
    }

    public void selectMember(Member member) {
        if (member != null) {
            selectedMember = member;
            selectedMemberFullName.setText(member.getFullName() + " [" + member.getId() + "]");
        }
    }

    @FXML
    private void clearBookFilter() {
        filterTextFieldBook.setText(null);
        bookTableView.getItems().clear();
        memberTableView.getItems().clear();
    }

    @FXML
    private void clearMemberFilter() {
        filterTextFieldMember.setText(null);
        memberTableView.getItems().clear();
        selectedMember = null;
    }

    @FXML
    protected void selectMemberButton() {
        selectMember(memberTableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    protected void findMembers() {
        if (filterTextFieldMember.getText() == null || filterTextFieldMember.getText().isEmpty()) {
            return;
        }

        try {
            ArrayList<Member> memberArrayList;
            if (MemberSearchController
                    .getFilterType(filterChoiceBoxMember.getSelectionModel().getSelectedIndex())
                    .getValue() == MemberSearchController.MemberSearchFilter.FULL_NAME.getValue()) {
                memberArrayList = CommonUtils.getMembersData("SELECT * FROM members WHERE full_name LIKE '%" + filterTextFieldMember.getText() + "%' COLLATE utf8_general_ci");
            } else {
                memberArrayList = CommonUtils.getFilteredMembers(filterTextFieldMember, filterChoiceBoxMember);
            }
            memberTableView.setItems(FXCollections.observableList(memberArrayList));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    protected void clearAllFields() {
        super.clearAllFields();
        memberTableView.getItems().clear();
        bookTableView.getItems().clear();
        filterTextFieldMember.setText(null);
        selectedMemberFullName.setText(null);
        datePicker.setValue(null);
        notes.setText(null);
        selectedMember = null;
    }

    protected abstract void bookTableViewOnClicked();

    @Override
    protected void initializeColumns() {
        bookIDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookSerialColumn.setCellValueFactory(new PropertyValueFactory<>("serial"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        bookAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        bookCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        bookLanguageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
        bookPublishYearColumn.setCellValueFactory(new PropertyValueFactory<>("publishYear"));
        bookPublishHouseColumn.setCellValueFactory(new PropertyValueFactory<>("publishHouse"));
        bookPublishCityColumn.setCellValueFactory(new PropertyValueFactory<>("publishCity"));
        bookPagesColumn.setCellValueFactory(new PropertyValueFactory<>("pages"));

        memberIDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        memberFullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        memberEMailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        memberPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        memberCourseColumn.setCellValueFactory(new PropertyValueFactory<>("course"));
    }
}
