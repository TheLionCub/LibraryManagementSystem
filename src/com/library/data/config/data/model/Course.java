package com.library.data.config.data.model;

import java.util.ArrayList;

public class Course {
    private Integer number;
    private ArrayList<String> letters;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public ArrayList<String> getLetters() {
        return letters;
    }

    public void setLetters(ArrayList<String> letters) {
        this.letters = letters;
    }

    public void addToLetters(String letter) {
        letters.add(letter);
    }
}
