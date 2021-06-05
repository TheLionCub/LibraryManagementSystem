package com.library.i18n;

import com.library.Main;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class I18nProvider {
    private static I18nProvider localization = null;

    public static final String defaultLocale = "en";

    public static final List<LocaleLanguage> localeLanguages = Arrays.asList(
            new LocaleLanguage("en", "English"),
            new LocaleLanguage("ru", "Русский")
    );

    private ResourceBundle resourceBundle;
    private Locale locale;

    public static I18nProvider getInstance() {
        if (localization == null) {
            localization = new I18nProvider();
            localization.setDefaultLocalization();
        }
        return localization;
    }

    private void setDefaultLocalization() {
        String configLanguage = Main.getLibraryConfig().getConfiguration().getLanguage();

        if (!localeLanguages.stream().map(LocaleLanguage::getKey).collect(Collectors.toList()).contains(configLanguage)) {
            setLocalization(defaultLocale);
            return;
        }

        setLocalization(configLanguage);
    }

    public void setLocalization(String language) {
        locale = new Locale(language);
        resourceBundle = ResourceBundle.getBundle("resources/lang/common", locale);

        Main.getLibraryConfig().getConfiguration().setLanguage(language);
        Main.getLibraryConfig().saveConfig();
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static I18nProvider getLocalization() {
        return localization;
    }

    public Locale getLocale() {
        return locale;
    }
}
