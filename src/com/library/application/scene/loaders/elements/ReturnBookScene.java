package com.library.application.scene.loaders.elements;

import com.library.application.controllers.ReturnBookController;
import com.library.application.scene.loaders.WindowManager;
import com.library.data.model.Book;
import com.library.data.model.Member;
import com.library.i18n.I18nProvider;

import java.io.IOException;
import java.net.URL;

public class ReturnBookScene extends WindowManager {
    private static final URL url = IssueBookScene.class.getClassLoader().getResource("resources/fxml/management/returnBook.fxml");

    private ReturnBookScene(URL url) throws IOException {
        super(url, I18nProvider.getLocalization().getResourceBundle().getString("taskbar.title.returnBook"), true, null);

        scene.getStylesheets().add(getClass().getResource("/resources/css/elements/returnBook.css").toString());

        stage.setWidth(800);
        stage.setHeight(1000);
    }

    public static ReturnBookScene load() throws IOException {
        return new ReturnBookScene(url);
    }

    public static ReturnBookScene loadWithData(Member member, Book book) throws IOException {
        ReturnBookScene returnBookScene = new ReturnBookScene(url);

        ReturnBookController returnBookController = returnBookScene.getFxmlLoader().getController();
        returnBookController.selectMember(member);
        returnBookController.selectBook(book);

        return returnBookScene;
    }
}
