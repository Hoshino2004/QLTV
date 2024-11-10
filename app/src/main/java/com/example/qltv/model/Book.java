package com.example.qltv.model;

import java.io.Serializable;

public class Book implements Serializable {
    private String id;
    private String name;
    private String image;
    private int quantity;
    private String author;
    private String category;
    private String description;

    private String status;

    public Book() {
    }

    public Book(String id, String name, String image, int quantity, String author, String category, String description, String status) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.quantity = quantity;
        this.author = author;
        this.category = category;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

