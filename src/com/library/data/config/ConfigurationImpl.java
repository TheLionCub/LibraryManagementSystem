package com.library.data.config;

import com.library.data.config.data.model.Course;

import java.util.ArrayList;

public class ConfigurationImpl implements Configuration {
    private String language;
    private ArrayList<Course> courses;
    private ArrayList<String> languages;
    private ArrayList<String> categories;
    private ArrayList<String> publishHouses;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<Course> courses) {
        this.courses = courses;
    }

    public ArrayList<String> getLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public ArrayList<String> getPublishHouses() {
        return publishHouses;
    }

    public void setPublishHouses(ArrayList<String> publishHouses) {
        this.publishHouses = publishHouses;
    }
}
