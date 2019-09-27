package com.peatis.elofy.model;

import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;

public class Cycle extends User {
    private String startAt;
    private String endAt;

    Cycle(JsonObject json) {
        super(json);
        setStartAt(Elofy.string(json, "inicio_vigencia"));
        setEndAt(Elofy.string(json, "fim_vigencia"));
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
}
