package com.peatis.elofy.model;

import com.annimon.stream.Stream;
import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Serializable {
    private int id;
    private int queryId;
    private String question;
    private Type type;
    private int escala;
    private List<Option> options;

    public Question(JsonObject json) {
        setId(Elofy.Int(json, "id_questionario"));
        setQueryId(Elofy.Int(json, "id_pergunta"));
        setQuestion(Elofy.string(json, "question"));
        setType(Type.of(Elofy.string(json, "type")));
        setEscala(Elofy.Int(json, "escala"));
        if (json.has("options")) {
            setOptions(Stream.of(json.getAsJsonArray("options")).map(item -> new Option(item.getAsJsonObject())).toList());
        } else {
            setOptions(new ArrayList<>());
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQueryId() {
        return queryId + "";
    }

    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getEscala() {
        return escala;
    }

    public void setEscala(int escala) {
        this.escala = escala;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public enum Type {
        TEXT("q"), STAR("e"), HEART("c"), MULTIPLE("o"), AGREE("f");

        final String value;

        Type(String value) {
            this.value = value;
        }

        static Type of(String value) {
            return Stream.of(Type.values()).filter(type -> type.value.equals(value)).single();
        }
    }

    public class Option implements Serializable {
        private int id;
        private String answer;
        private double percentage;

        Option(JsonObject json) {
            setId(Elofy.Int(json, "id"));
            setAnswer(Elofy.string(json, "answer"));
            setPercentage(Elofy.Double(json, "percentage"));
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }
}
