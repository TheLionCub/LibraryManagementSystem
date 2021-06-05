package com.library.data.model;

import com.library.data.RowItem;

import javax.persistence.Column;

public class Book implements RowItem {
    @Column(name = "id")
    private final Integer id;
    @Column(name = "serial")
    private final String serial;
    @Column(name = "title")
    private final String title;
    @Column(name = "author")
    private final String author;
    @Column(name = "description")
    private final String description;
    @Column(name = "category")
    private final String category;
    @Column(name = "language")
    private final String language;
    @Column(name = "publish_year")
    private final String publishYear;
    @Column(name = "publish_house")
    private final String publishHouse;
    @Column(name = "publish_city")
    private final String publishCity;
    @Column(name = "pages")
    private final Integer pages;
    @Column(name = "member_id")
    private final String memberID;
    @Column(name = "rent_deadline")
    private final Long rentDeadline;
    @Column(name = "rent_notes")
    private final String rentNotes;
    @Column(name = "condition")
    private final String condition;
    @Column(name = "notes")
    private final String notes;
    @Column(name = "created")
    private final Long created;
    @Column(name = "last_updated")
    private final Long lastUpdated;

    public Book(Integer id,
                String serial,
                String title,
                String author,
                String description,
                String category,
                String language,
                String publishYear,
                String publishHouse,
                String publishCity,
                Integer pages,
                String memberID,
                Long rentDeadline,
                String rentNotes,
                String condition,
                String notes,
                Long created,
                Long lastUpdated) {
        this.id = id;
        this.serial = serial;
        this.title = title;
        this.author = author;
        this.description = description;
        this.category = category;
        this.language = language;
        this.publishYear = publishYear;
        this.publishHouse = publishHouse;
        this.publishCity = publishCity;
        this.pages = pages;
        this.memberID = memberID;
        this.rentDeadline = rentDeadline;
        this.rentNotes = rentNotes;
        this.condition = condition;
        this.notes = notes;
        this.created = created;
        this.lastUpdated = lastUpdated;
    }

    public Integer getId() {
        return id;
    }

    public String getSerial() {
        return serial;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }

    public String getPublishYear() {
        return publishYear;
    }

    public String getPublishHouse() {
        return publishHouse;
    }

    public String getPublishCity() {
        return publishCity;
    }

    public Integer getPages() {
        return pages;
    }

    public Boolean getAvailable() {
        return memberID == null || memberID.equals("");
    }

    public String getMemberID() {
        return memberID;
    }

    public Long getRentDeadline() {
        return rentDeadline;
    }

    public String getRentNotes() {
        return rentNotes;
    }

    public String getCondition() {
        return condition;
    }

    public String getNotes() {
        return notes;
    }

    public Long getCreated() {
        return created;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public String toString() {
        return getId() + " | " + getSerial() + " | " + getTitle();
    }
}
