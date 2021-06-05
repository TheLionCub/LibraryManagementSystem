package com.library.utils;

import com.library.data.config.data.model.Course;

import java.util.ArrayList;

public class ConfigurationUtils {
    public static ArrayList<String> getCoursesAsList(ArrayList<Course> courses) {
        ArrayList<String> arrayList = new ArrayList<>();

        for (Course course : courses) {
            for (String letter : course.getLetters()) {
                arrayList.add(course.getNumber().toString() + letter);
            }
        }

        return arrayList;
    }
}
