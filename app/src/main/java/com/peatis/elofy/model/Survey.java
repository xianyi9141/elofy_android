package com.peatis.elofy.model;

import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;

import java.io.Serializable;

public class Survey implements Serializable {
    private int id;
    private int questionId;
    private String name;
    private String userName;
    private boolean active;
    private boolean answered;
    private int count;
    private String timestamp;

    public Survey(JsonObject json) {
        setId(Elofy.Int(json, "id_pesquisa"));
        setQuestionId(Elofy.Int(json, "id_questionario"));
        setName(Elofy.string(json, "nome_pesquisa"));
        setUserName(Elofy.string(json, "nome_usuario"));
        setActive(Elofy.Int(json, "ativo") == 1);
        setAnswered(Elofy.Int(json, "respondida") == 1);
        setCount(Elofy.Int(json, "surveys"));
        setTimestamp(Elofy.string(json, "data_atualizacao"));
    }

    public String getId() {
        return id + "";
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId + "";
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public String getCount() {
        return count + " pessoas responderam";
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
