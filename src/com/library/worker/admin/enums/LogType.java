package com.library.worker.admin.enums;

import com.library.utils.EnumUtils;

public enum LogType implements EnumUtils.TInterface {
    UNKNOWN(-1, "enum.logType.unknown"),
    CREATE(0, "enum.logType.create"),
    DELETE(1, "enum.logType.delete"),
    UPDATE(2, "enum.logType.update"),
    ISSUE(3, "enum.logType.issue"),
    RETURN(4, "enum.logType.return");

    private final int index;
    private final String text;

    LogType(int index, String text) {
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