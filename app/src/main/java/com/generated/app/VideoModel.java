package com.generated.app;

import android.net.Uri;

public class VideoModel {
    public long id;
    public String title;
    public String path;
    public String duration;
    public String size;
    public String folder;
    public int viewCount;
    public long lastViewed;

    public VideoModel(long id, String title, String path, String duration, String size, String folder) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.size = size;
        this.folder = folder;
    }
}
