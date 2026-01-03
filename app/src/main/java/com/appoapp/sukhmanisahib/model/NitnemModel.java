package com.appoapp.sukhmanisahib.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class NitnemModel implements Parcelable {

    private Map<String, String> title;
    private Map<String, String> description;
    private Map<String, String> path;
    private String pathAudioLink; // 🔥 AUDIO URL

    // 🔹 REQUIRED empty constructor (Firestore)
    public NitnemModel() {}

    // 🔹 Parcelable constructor
    protected NitnemModel(Parcel in) {

        title = new HashMap<>();
        in.readMap(title, String.class.getClassLoader());

        description = new HashMap<>();
        in.readMap(description, String.class.getClassLoader());

        path = new HashMap<>();
        in.readMap(path, String.class.getClassLoader());

        pathAudioLink = in.readString(); // 🔥
    }

    public static final Creator<NitnemModel> CREATOR = new Creator<NitnemModel>() {
        @Override
        public NitnemModel createFromParcel(Parcel in) {
            return new NitnemModel(in);
        }

        @Override
        public NitnemModel[] newArray(int size) {
            return new NitnemModel[size];
        }
    };

    // 🔹 Firestore getters/setters
    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public Map<String, String> getPath() {
        return path;
    }

    public void setPath(Map<String, String> path) {
        this.path = path;
    }

    public String getPathAudioLink() {
        return pathAudioLink;
    }

    public void setPathAudioLink(String pathAudioLink) {
        this.pathAudioLink = pathAudioLink;
    }

    // 🔹 UI helpers (dynamic language)
    public String getTitle(String lang) {
        return title != null && title.containsKey(lang) ? title.get(lang) : "";
    }

    public String getDescription(String lang) {
        return description != null && description.containsKey(lang)
                ? description.get(lang) : "";
    }

    public String getPath(String lang) {
        return path != null && path.containsKey(lang)
                ? path.get(lang) : "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(title);
        dest.writeMap(description);
        dest.writeMap(path);
        dest.writeString(pathAudioLink); // 🔥
    }
}
