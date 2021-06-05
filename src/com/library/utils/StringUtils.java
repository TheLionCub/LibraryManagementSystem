package com.library.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class StringUtils {
    public static String trimIfNotNull(String string) {
        if (string != null) {
            string = string.trim();
        }
        return string;
    }

    public static String trimMultilineStr(String input) {
        BufferedReader reader = new BufferedReader(new StringReader(input));
        StringBuilder result = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line.trim());
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
