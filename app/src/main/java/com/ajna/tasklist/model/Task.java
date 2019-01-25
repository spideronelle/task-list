package com.ajna.tasklist.model;

import java.io.Serializable;

public class Task implements Serializable{
    public static final long serialVersionUID = 20211228L;

    private int id;
    private String title;
    private String details;
    private int categoryId;

    public Task(int id, String title, String details, int categoryId) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}