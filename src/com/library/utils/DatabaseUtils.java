package com.library.utils;

import java.util.Arrays;
import java.util.Iterator;

public class DatabaseUtils {
    public static String getAnyColumnQueryPart(String value, String[] columns, boolean strict) {
        StringBuilder queryPart = new StringBuilder();

        Iterator<String> iterator = Arrays.stream(columns).iterator();
        int index = 0;
        while (iterator.hasNext()) {
            index++;
            if (strict) {
                queryPart.append(iterator.next()).append("='").append(value).append("'");
            } else {
                queryPart.append(iterator.next()).append(" LIKE '%").append(value).append("%'");
            }
            if (columns.length >= index + 1) {
                queryPart.append(" OR ");
            }
        }

        return queryPart.toString();
    }
}
