package com.javafx.custom.components;

import javafx.beans.NamedArg;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

public class DBTableColumn<S, T> extends TableColumn<S, T> {
    private String dbPropertyName;
    private TextField searchFieldBind;

    public DBTableColumn(@NamedArg("dbPropertyName") final String dbPropertyName) {
        super();

        this.dbPropertyName = dbPropertyName;
    }

    public String getDbPropertyName() {
        return dbPropertyName;
    }

    public void setDbPropertyName(String dbPropertyName) {
        this.dbPropertyName = dbPropertyName;
    }

    public TextField getSearchFieldBind() {
        return searchFieldBind;
    }

    public void setSearchFieldBind(TextField searchFieldBind) {
        this.searchFieldBind = searchFieldBind;
    }
}
