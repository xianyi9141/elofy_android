package com.peatis.elofy.model;

import com.google.gson.JsonObject;
import com.peatis.elofy.Elofy;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private int companyId;
    private String name;
    private String image;
    private String image50px;
    private String image150px;

    public User(JsonObject json) {
        setId(Elofy.Int(json, "id"));
        setCompanyId(Elofy.Int(json, "id_empresa"));
        setName(!json.has("name") || json.get("name").isJsonNull() ? (Elofy.string(json, "nome")) : json.get( "name").getAsString());
        setImage(!json.has("image") || json.get("image").isJsonNull() ? (Elofy.string(json, "orignal_image")) : json.get("image").getAsString());
        setImage50px(Elofy.string(json, "xs_image"));
        setImage150px(Elofy.string(json, "md_image"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
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

    public String getImage50px() {
        return image50px;
    }

    public void setImage50px(String image50px) {
        this.image50px = image50px;
    }

    public String getImage150px() {
        return image150px;
    }

    public void setImage150px(String image150px) {
        this.image150px = image150px;
    }
}
