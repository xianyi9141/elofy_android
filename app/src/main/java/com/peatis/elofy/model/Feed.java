package com.peatis.elofy.model;

import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;
import com.peatis.elofy.R;

public class Feed {
    private int id;
    private Type type;
    private String text;
    private String timestamp;
    private long timeDiff;

    public Feed(JsonObject json) {
        setId(Elofy.Int(json, "id"));
        setType(Type.valueOf(Elofy.Int(json, "type")));
        setText(Elofy.string(json, "event"));
        setTimestamp(Elofy.string(json, "date"));
        setTimeDiff(Elofy.Long(json, "diff"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimeDiff() {
        return Elofy.dateDiff(timeDiff);
    }

    public void setTimeDiff(long timeDiff) {
        this.timeDiff = timeDiff;
    }

    public enum Type {
        WARNING(0), SUCCESS(1), INFO(2);

        final int value;

        Type(int value) {
            this.value = value;
        }

        static Type valueOf(int value) {
            switch (value) {
                case 0:
                    return WARNING;
                case 1:
                    return SUCCESS;
                default:
                    return INFO;
            }
        }

        public int color() {
            switch (this) {
                case WARNING:
                    return R.color.red;
                case SUCCESS:
                    return R.color.green;
                case INFO:
                    return R.color.blue;
            }
            return R.color.blue;
        }
    }
}
