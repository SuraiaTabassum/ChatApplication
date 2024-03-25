package com.example.chatapplication.adapter;

import java.util.ArrayList;

public class Comments {

    private String comment;
    private String commentBy;
    private long timeStamp;
    private String userProfileUri;
    private ArrayList<String> likedBy;
    private ArrayList<String> dislikedBy;

    public Comments() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Comments(String comment, String commentBy, long timeStamp, String userProfileUri) {
        this.comment = comment;
        this.commentBy = commentBy;
        this.timeStamp = timeStamp;
        this.userProfileUri = userProfileUri;
        this.likedBy = new ArrayList<>();
        this.dislikedBy = new ArrayList<>();
    }

    public String getComment() {
        return comment;
    }

    public String getCommentBy() {
        return commentBy;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getUserProfileUri() {
        return userProfileUri;
    }

    public ArrayList<String> getLikedBy() {
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        return likedBy;
    }


    public ArrayList<String> getDislikedBy() {

        if (dislikedBy == null) {
            dislikedBy = new ArrayList<>();
        }
        return dislikedBy;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCommentBy(String commentBy) {
        this.commentBy = commentBy;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setUserProfileUri(String userProfileUri) {
        this.userProfileUri = userProfileUri;
    }

    public void setLikedBy(ArrayList<String> likedBy) {
        this.likedBy = likedBy;
    }

    public void setDislikedBy(ArrayList<String> dislikedBy) {
        this.dislikedBy = dislikedBy;
    }
}
