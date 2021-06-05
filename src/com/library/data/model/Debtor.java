package com.library.data.model;

public class Debtor {
    private final Member member;
    private final Book book;
    private final Long rentDeadline;

    public Debtor(Member member, Book book, Long rentDeadline) {
        this.member = member;
        this.book = book;
        this.rentDeadline = rentDeadline;
    }

    public Member getMember() {
        return member;
    }

    public Book getBook() {
        return book;
    }

    public Long getRentDeadline() {
        return rentDeadline;
    }
}
