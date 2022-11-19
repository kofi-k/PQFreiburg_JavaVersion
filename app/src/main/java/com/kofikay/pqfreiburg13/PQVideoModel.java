package com.kofikay.pqfreiburg13;

public class PQVideoModel {
    private String title, videoId, url;

    public PQVideoModel(String videoId, String url) {
        this.videoId = videoId;
        this.url = url;
    }

    public PQVideoModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
