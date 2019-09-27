package com.peatis.elofy.model;

import com.annimon.stream.Stream;
import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;

import java.io.Serializable;
import java.util.List;

public class Elos implements Serializable {
    private int id;
    private User sender;
    private List<User> receiver;
    private String description;
    private String timestamp;
    private long timeDiff;
    private List<Comment> comments;
    private int commentCnt;
    private int likeCnt;
    private boolean liked;
    private boolean expanded = false;

    public Elos(JsonObject json) {
        setId(Elofy.Int(json, "id"));
        setSender(new User(json.getAsJsonObject("usuario_responsavel")));
        setReceiver(Stream.of(json.getAsJsonArray("usuarios_mencionados")).map(elt -> new User(elt.getAsJsonObject())).toList());
        setDescription(Elofy.string(json, "descricao_elogio"));
        setTimestamp(Elofy.string(json, "data_atualizacao"));
        setTimeDiff(Elofy.Long(json, "diff"));
        setCommentCnt(Elofy.Int(json, "total_comment"));
        setLikeCnt(Elofy.Int(json, "total_likes"));
        setLiked(Elofy.Int(json, "i_liked") == 1);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public List<User> getReceiver() {
        return receiver;
    }

    public void setReceiver(List<User> receiver) {
        this.receiver = receiver;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getCommentCnt() {
        return commentCnt;
    }

    public void setCommentCnt(int commentCnt) {
        this.commentCnt = commentCnt;
    }

    public int getLikeCnt() {
        return likeCnt;
    }

    public void setLikeCnt(int likeCnt) {
        this.likeCnt = likeCnt;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void increaseLikeCnt() {
        this.likeCnt++;
    }

    public void increaseCommentCnt() {
        this.commentCnt++;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
