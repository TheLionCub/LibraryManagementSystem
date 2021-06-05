package com.library.data.model;

import com.library.data.RowItem;

import javax.persistence.Column;

public class Member implements RowItem {
    @Column(name = "id")
    private final Integer id;
    @Column(name = "full_name")
    private final String fullName;
    @Column(name = "email")
    private final String email;
    @Column(name = "phone")
    private final String phone;
    @Column(name = "course")
    private final String course;
    @Column(name = "address")
    private final String address;
    @Column(name = "notes")
    private final String notes;
    @Column(name = "birth_date")
    private final String birthDate;
    @Column(name = "created")
    private final Long created;
    @Column(name = "last_updated")
    private final Long lastUpdated;

    public Member(Integer id,
                  String fullName,
                  String email,
                  String phone,
                  String course,
                  String address,
                  String notes,
                  String birthDate,
                  Long created,
                  Long lastUpdated) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.course = course;
        this.address = address;
        this.notes = notes;
        this.birthDate = birthDate;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    public Integer getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCourse() {
        return course;
    }

    public String getAddress() {
        return address;
    }

    public String getNotes() {
        return notes;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public Long getCreated() {
        return created;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public String toString() {
        return getId() + " | " + getFullName();
    }
}
