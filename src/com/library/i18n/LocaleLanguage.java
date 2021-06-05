package com.library.i18n;

import java.util.Locale;

public class LocaleLanguage {
    private final String key;
    private final String name;
    private final Locale locale;

    LocaleLanguage(String key, String name) {
        this.key = key;
        this.name = name;

        this.locale = new Locale(key);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
