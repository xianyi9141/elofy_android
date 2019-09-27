package com.peatis.elofy.model;

import com.annimon.stream.Stream;
import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Objective implements Serializable {
    private int id;
    private int parentId;
    private String title;
    private String description;
    private Status status;
    private Type type;
    private Color color;
    private double percentage;
    private User user;
    private User team;
    private List<Cycle> cycles;
    private String timestamp;
    private String year;
    private List<KeyResult> keyResults;
    private String raw;

    public Objective(JsonObject json) {
        setId(Elofy.Int(json, "id"));
        setParentId(Elofy.Int(json, "parent_id"));
        setTitle(Elofy.string(json, "title"));
        setDescription(Elofy.string(json, "description"));
        setStatus(Status.of(Elofy.Int(json, "status")));
        setType(Type.of(Elofy.string(json, "type")));
        setColor(Color.of(Elofy.Int(json, "color")));
        setPercentage(Elofy.Double(json, "percentage"));
        if (json.has("user") && json.get("user").isJsonObject()) {
            setUser(new User(json.get("user").getAsJsonObject()));
        }
        if (json.has("team") && json.get("team").isJsonObject()) {
            setTeam(new User(json.get("team").getAsJsonObject()));
        }
        if (json.has("cycles") && json.get("cycles").isJsonArray()) {
            setCycles(Stream.of(json.get("cycles").getAsJsonArray()).map(elt -> new Cycle(elt.getAsJsonObject())).toList());
        } else {
            setCycles(new ArrayList<>());
        }
        setTimestamp(Elofy.string(json, "end"));
        setYear(Elofy.string(json, "year"));

        if (json.has("keys") && json.get("keys").isJsonArray()) {
            setKeyResults(Stream.of(json.get("keys").getAsJsonArray()).map(key -> new KeyResult(key.getAsJsonObject())).toList());
        }
        setRaw(json.has("keys") ? json.toString().toLowerCase() : "");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getTeam() {
        return team;
    }

    public void setTeam(User team) {
        this.team = team;
    }

    public List<Cycle> getCycles() {
        return cycles;
    }

    public void setCycles(List<Cycle> cycles) {
        this.cycles = cycles;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<KeyResult> getKeyResults() {
        return keyResults;
    }

    public void setKeyResults(List<KeyResult> keyResults) {
        this.keyResults = keyResults;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    enum Status {
        PENDING(0), DONE(1), PROGRESS(2), CLOSE(3), NONE(4);

        final int value;

        Status(int value) {
            this.value = value;
        }

        static Status of(int value) {
            switch (value) {
                case 0:
                    return PENDING;
                case 1:
                    return DONE;
                case 2:
                    return PROGRESS;
                case 3:
                    return CLOSE;
                default:
                    return NONE;
            }
        }
    }

    public enum Type {
        SHARED("c"), INDIVIDUAL("i"), TEAM("t"), TEAM_NAME("");

        final String value;

        Type(String value) {
            this.value = value;
        }

        static Type of(String value) {
            if (SHARED.value.equals(value)) return SHARED;
            else if (INDIVIDUAL.value.equals(value)) return INDIVIDUAL;
            else if (TEAM.value.equals(value)) return TEAM;
            else return TEAM_NAME;
        }

        public int image() {
            switch (this) {
                case SHARED:
                    return R.drawable.btn_share;
                case INDIVIDUAL:
                    return R.drawable.btn_individual;
                default:
                    return R.drawable.btn_team;
            }
        }

        public int color() {
            switch (this) {
                case SHARED:
                    return R.color.orange;
                case INDIVIDUAL:
                    return R.color.green;
                case TEAM:
                    return R.color.red;
                default:
                    return R.color.blue;
            }
        }
    }

    public enum Color {
        NONE(0), ONTRACK(1), ATTENTION(2), DELAYED(3);

        public final int value;

        Color(int value) {
            this.value = value;
        }

        static Color of(int value) {
            switch (value) {
                case 1:
                    return ONTRACK;
                case 2:
                    return ATTENTION;
                case 3:
                    return DELAYED;
                default:
                    return NONE;
            }
        }

        public int progressDrawable() {
            switch (this) {
                case ONTRACK:
                    return R.drawable.progress_blue;
                case ATTENTION:
                    return R.drawable.progress_orange;
                case DELAYED:
                    return R.drawable.progress_red;
                default:
                    return R.drawable.progress_default;
            }
        }
    }
}
