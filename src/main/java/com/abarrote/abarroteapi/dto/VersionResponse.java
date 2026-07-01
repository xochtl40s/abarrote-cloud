package com.abarrote.abarroteapi.dto;

public class VersionResponse {

    private String application;
    private String version;
    private String author;

    public VersionResponse(String application, String version, String author) {
        this.application = application;
        this.version = version;
        this.author = author;
    }

    public String getApplication() {
        return application;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }
}