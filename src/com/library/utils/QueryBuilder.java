package com.library.utils;

public class QueryBuilder {
    private String query = "";

    public QueryBuilder() {
    }

    public void addIfExists(String column, String value) {
        if (value != null) {
            query += column + "='" + value + "',";
        }
    }

    public void addIfExists(String column, Integer value) {
        if (value != null) {
            query += column + "=" + value + ",";
        }
    }

    public void addIfExists(String column, Long value) {
        if (value != null) {
            query += column + "=" + value + ",";
        }
    }

    public void setEnd() {
        if (query.endsWith(",")) {
            query = query.substring(0, query.length() - 1) + " ";
        }
    }

    public void add(String value) {
        query += value + " ";
    }

    public String getQuery() {
        return query;
    }
}
