package com.library.data.config;

public class DBConfigurationImpl implements Configuration {
    private Integer version;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
