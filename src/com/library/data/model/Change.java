package com.library.data.model;

import com.library.data.RowItem;
import com.library.worker.admin.enums.LogTarget;
import com.library.worker.admin.enums.LogType;

import javax.persistence.Column;

public class Change implements RowItem {
    @Column(name = "id")
    private final Integer id;
    @Column(name = "executor_id")
    private final Integer executorID;
    @Column(name = "username")
    private final String username;
    @Column(name = "full_name")
    private final String fullName;
    @Column(name = "type")
    private final LogType type;
    @Column(name = "target")
    private final LogTarget target;
    @Column(name = "details")
    private final String details;
    @Column(name = "timestamp")
    private final long timestamp;
    private final String datetime;

    public Change(Integer id,
                  Integer executorID,
                  String username,
                  String fullName,
                  LogType type,
                  LogTarget target,
                  String details,
                  long timestamp,
                  String datetime) {
        this.id = id;
        this.executorID = executorID;
        this.username = username;
        this.fullName = fullName;
        this.type = type;
        this.target = target;
        this.details = details;
        this.timestamp = timestamp;
        this.datetime = datetime;
    }

    public Integer getId() {
        return id;
    }

    public Integer getExecutorID() {
        return executorID;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public LogType getType() {
        return type;
    }

    public LogTarget getTarget() {
        return target;
    }

    public String getDetails() {
        return details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDatetime() {
        return datetime;
    }
}
