package com.example.blog.models;

import com.google.firebase.database.ServerValue;

public class Post {
    private String postKey;
    private String Title;
    private String Description;
    private String Picture;
    private String UserID;
    private String UserPhoto;
    private Object TimeStamp;

    public Post() {
    }

    public Post(String title, String description, String picture, String userID, String userPhoto) {
        Title = title;
        Description = description;
        Picture = picture;
        UserID = userID;
        UserPhoto = userPhoto;
        TimeStamp = ServerValue.TIMESTAMP;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUserPhoto() {
        return UserPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        UserPhoto = userPhoto;
    }

    public Object getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        TimeStamp = timeStamp;
    }
}
