package com.library.utils;

public class EnumUtils {
    public static <T extends Enum<T> & TInterface> T getEnumIDValue(Class<T> enumType, int id) {
        for (T t : enumType.getEnumConstants()) {
            if (t.getIndex() == id) {
                return t;
            }
        }
        return null;
    }

    public interface TInterface {
        int getIndex();

        String getText();
    }
}
