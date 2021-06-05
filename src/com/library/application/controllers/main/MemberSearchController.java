package com.library.application.controllers.main;

import com.javafx.custom.components.DBTableColumn;
import com.library.application.controllers.IssueBookController;
import com.library.application.controllers.MainController;
import com.library.application.controllers.ReturnBookController;
import com.library.application.controllers.abstractions.AbstractPaginationTableController;
import com.library.application.scene.loaders.elements.IssueBookScene;
import com.library.application.scene.loaders.elements.ReturnBookScene;
import com.library.cache.ControllersCache;
import com.library.data.model.Book;
import com.library.data.model.Member;
import com.library.utils.CommonUtils;
import com.library.utils.ControllerUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public final class MemberSearchController extends AbstractPaginationTableController<Member> {
    @FXML
    private DBTableColumn<Member, Integer> idColumn;
    @FXML
    private DBTableColumn<Member, String> fullNameColumn;
    @FXML
    private DBTableColumn<Member, String> emailColumn;
    @FXML
    private DBTableColumn<Member, String> phoneColumn;
    @FXML
    private DBTableColumn<Member, String> courseColumn;
    @FXML
    private DBTableColumn<Member, String> birthDateColumn;

    public static MemberSearchController getInstance() {
        return ControllersCache.memberSearchController;
    }

    private MainController mainController;

    public void injectMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        if (ControllersCache.memberSearchController == null) {
            ControllersCache.memberSearchController = this;
        }

        setDefaultTableFactory();

        try {
            data = CommonUtils.getMembersData("SELECT * FROM members");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        lateInit();
        checkTableItems();
    }

    public static MemberSearchFilter getFilterType(int index) {
        return MemberSearchFilter.values()[index];
    }

    @Override
    protected void initializeColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        courseColumn.setCellValueFactory(new PropertyValueFactory<>("course"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
    }

    @Override
    protected void addRestMenuItems(ContextMenu contextMenu, Member item) {
        MenuItem issueBookItem = new MenuItem(resourceBundle.getString("button.issueBook"));
        MenuItem returnBookItem = new MenuItem(resourceBundle.getString("button.returnBook"));
        MenuItem showHeldBooks = new MenuItem(resourceBundle.getString("button.showHeldBooks"));

        contextMenu.getItems().add(issueBookItem);

        ArrayList<Book> memberHeldBooks = null;
        try {
            memberHeldBooks = CommonUtils.getHeldMemberBooks(item);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        if (memberHeldBooks != null) {
            returnBookItem.setDisable(memberHeldBooks.size() == 0);
            showHeldBooks.setDisable(memberHeldBooks.size() == 0);

            contextMenu.getItems().add(returnBookItem);
            contextMenu.getItems().add(showHeldBooks);
        }

        issueBookItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    IssueBookScene issueBookScene = IssueBookScene.load();
                    IssueBookController issueBookController = issueBookScene.getFxmlLoader().getController();
                    issueBookController.setMemberData(item);
                    issueBookScene.getStage().show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        returnBookItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadBookScene(item);
            }
        });

        showHeldBooks.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadBookScene(item);
            }
        });
    }

    public static void loadBookScene(Member item) {
        ReturnBookScene returnBookScene = null;
        try {
            returnBookScene = ReturnBookScene.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (returnBookScene != null) {
            ReturnBookController returnBookController = returnBookScene.getFxmlLoader().getController();
            returnBookController.selectMember(item);

            returnBookScene.getStage().show();
        }
    }

    @Override
    protected void setFieldFilterDataClass(String query) throws SQLException {
        ArrayList<Member> arrayList = CommonUtils.getMembersData(query);

        setItems(arrayList);
    }

    @Override
    protected String getDbTableName() {
        return "members";
    }

    @Override
    protected void redirectToDetails(Member member) {
        ControllerUtils.redirectToMemberDetails(member);
    }

    public enum MemberSearchFilter {
        EMPTY(0),
        ID(1),
        FULL_NAME(2);

        private final int value;

        MemberSearchFilter(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}