package com.library.worker.admin.enums;

import com.library.utils.EnumUtils;

public enum LogTarget implements EnumUtils.TInterface {
    UNKNOWN(-1, "enum.logTarget.unknown"),
    BOOK(0, "enum.logTarget.book"),
    MEMBER(1, "enum.logTarget.member"),
    SETTINGS(2, "enum.logTarget.settings"),
    SYSTEM(3, "enum.logTarget.system");

    private final int index;
    private final String text;

    LogTarget(int index, String text) {
        this.index = index;
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }
}