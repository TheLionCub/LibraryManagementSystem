package com.library;

import com.library.i18n.I18nProvider;

import java.util.ResourceBundle;

public abstract class LibraryBaseAbstract {
    protected final I18nProvider localization = I18nProvider.getInstance();
    protected final ResourceBundle resourceBundle = localization.getResourceBundle();
}
