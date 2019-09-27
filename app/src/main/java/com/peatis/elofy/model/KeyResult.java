package com.peatis.elofy.model;

import com.annimon.stream.Stream;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;

import java.io.Serializable;
import java.util.List;

public class KeyResult implements Serializable {
    private int id;
    private String title;
    private User user;
    private double from;
    private double to;
    private double actual;
    private double percentage;
    private String unit;
    private String timestamp;
    private List<Activity> activities;
    private boolean expanded = false;

    public KeyResult(JsonObject json) {
        setId(Elofy.Int(json, "id"));
        setTitle(Elofy.string(json, "title"));
        setUser(new User(json));
        setFrom(Elofy.Double(json, "de"));
        setTo(Elofy.Double(json, "para"));
        setActual(Elofy.Double(json, "atual"));
        setPercentage(Elofy.Double(json, "percentage"));
        setTimestamp(Elofy.string(json, "last_date"));
        setUnit(Elofy.string(json, "measurement"));
        setUser(new User(json.get("user").getAsJsonObject()));

        if (json.get("activities").isJsonArray()) {
            JsonArray array = json.get("activities").getAsJsonArray();
            setActivities(Stream.of(array).map(elt -> new Activity(elt.getAsJsonObject())).toList());
        }
        setExpanded(!getActivities().isEmpty());
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getFrom() {
        return from;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }

    public double getActual() {
        return actual;
    }

    public void setActual(double actual) {
        this.actual = actual;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
