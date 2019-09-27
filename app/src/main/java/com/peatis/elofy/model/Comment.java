package com.peatis.elofy.model;

import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;

import java.io.Serializable;

public class Comment implements Serializable {
    private int id;
    private String username;
    private String text;

    public Comment(JsonObject json) {
        setId(Elofy.Int(json, "id"));
        setUsername(Elofy.string(json, "username"));
        setText(Elofy.string(json, "comment_text"));
    }

    public Comment(int id, String username, String text) {
        this.id = id;
        this.username = username;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
