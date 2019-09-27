package com.peatis.elofy.model;

import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;

import java.io.Serializable;

public class Activity implements Serializable {
    private int id;
    private String title;
    private String description;
    private double percentage;
    private int atraso;
    private String startAt;
    private String endAt;
    private User user;

    public Activity(JsonObject json) {
        setId(Elofy.Int(json, "id"));
        setTitle(Elofy.string(json, "title"));
        setDescription(Elofy.string(json, "description"));
        setPercentage(Elofy.Double(json, "percentage"));
        setAtraso(Elofy.Int(json, "atraso"));
        setStartAt(Elofy.string(json, "init"));
        setEndAt(!json.has("fim") || json.get("fim").isJsonNull() ? (Elofy.string(json, "end")) : json.get("fim").getAsString());
        setUser(new User(json.has("user") ? json.get("user").getAsJsonObject() : json));
    }

    public Activity.Status status() {
        if (getPercentage() >= 0 && getPercentage() < 50 && getAtraso() == 0)
            return Status.PENDING;
        else if (getPercentage() >= 50 && getPercentage() < 100 && getAtraso() == 0)
            return Status.PROGRESS;
        else if (getPercentage() == 100 && getAtraso() == 0)
            return Status.DONE;
        else if (getPercentage() <= 50 && getAtraso() == 1)
            return Status.LATE;
        else
            return Status.DONE;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getAtraso() {
        return atraso;
    }

    public void setAtraso(int atraso) {
        this.atraso = atraso;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public enum Status {
        PENDING(0), DONE(1), LATE(2), PROGRESS(3);

        final int value;

        Status(int value) {
            this.value = value;
        }

        public String string() {
            switch (this) {
                case PENDING:
                    return "Pendente";
                case PROGRESS:
                    return "Em andamento";
                case LATE:
                    return "Atrasada";
                default:
                    return "Concluída";
            }
        }

        public int color() {
            switch (this) {
                case PENDING:
                    return R.color.orange;
                case PROGRESS:
                    return R.color.blue;
                case LATE:
                    return R.color.red;
                default:
                    return R.color.green;
            }
        }

        public int image() {
            switch (this) {
                case PENDING:
                    return R.drawable.btn_pending;
                case PROGRESS:
                    return R.drawable.btn_progress;
                case LATE:
                    return R.drawable.btn_late;
                default:
                    return R.drawable.btn_done;
            }
        }
    }
}