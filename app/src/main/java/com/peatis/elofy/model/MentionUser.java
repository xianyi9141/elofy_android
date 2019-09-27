package com.peatis.elofy.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.linkedin.android.spyglass.mentions.Mentionable;
import com.peatis.elofy.Elofy;

import androidx.annotation.NonNull;

public class MentionUser implements Mentionable {
    public static final Parcelable.Creator<MentionUser> CREATOR = new Parcelable.Creator<MentionUser>() {
        public MentionUser createFromParcel(Parcel in) {
            return new MentionUser(in);
        }

        public MentionUser[] newArray(int size) {
            return new MentionUser[size];
        }
    };

    private int id;
    private String name;


    public MentionUser(JsonObject json) {
        setId(Elofy.Int(json, "id"));
        setName(!json.has("name") || json.get("name").isJsonNull() ? (Elofy.string(json, "nome")) : json.get("name").getAsString());
    }

    public MentionUser(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String getTextForDisplayMode(MentionDisplayMode mode) {
        return getName();
    }

    @Override
    public MentionDeleteStyle getDeleteStyle() {
        return MentionDeleteStyle.PARTIAL_NAME_DELETE;
    }

    @Override
    public int getSuggestibleId() {
        return getId();
    }

    @Override
    public String getSuggestiblePrimaryText() {
        return getName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(getName());
    }
}
