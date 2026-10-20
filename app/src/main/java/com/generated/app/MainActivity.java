package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private LinearLayout videoContainer;
    private LinearLayout recommendationContainer;
    private List<VideoModel> allVideos = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        videoContainer = findViewById(R.id.video_container);
        recommendationContainer = findViewById(R.id.recommendation_container);
        ImageButton btnSettings = findViewById(R.id.btn_settings);

        btnSettings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        } else {
            loadVideos();
        }
    }

    private void loadVideos() {
        allVideos.clear();
        videoContainer.removeAllViews();
        recommendationContainer.removeAllViews();

        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                long durationMs = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                
                String folder = "Media";
                File file = new File(path);
                if (file.getParentFile() != null) folder = file.getParentFile().getName();

                String duration = String.format("%02d:%02d", 
                        TimeUnit.MILLISECONDS.toMinutes(durationMs),
                        TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMs)));

                VideoModel video = new VideoModel(id, title, path, duration, String.valueOf(size), folder);
                allVideos.add(video);
                addVideoToLayout(video, videoContainer);
            } while (cursor.moveToNext());
            cursor.close();
        }

        buildRecommendations();
        
        if (allVideos.isEmpty()) {
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        }
    }

    private void buildRecommendations() {
        List<Long> recommendedIds = dbHelper.getRecommendedIds();
        for (Long id : recommendedIds) {
            for (VideoModel v : allVideos) {
                if (v.id == id) {
                    addVideoToLayout(v, recommendationContainer);
                    break;
                }
            }
        }
    }

    private void addVideoToLayout(final VideoModel video, LinearLayout container) {
        View view = LayoutInflater.from(this).inflate(R.layout.video_item, container, false);
        TextView title = view.findViewById(R.id.video_title);
        TextView info = view.findViewById(R.id.video_info);
        ImageView thumb = view.findViewById(R.id.video_thumbnail);

        title.setText(video.title);
        info.setText(video.folder + " • " + video.duration);
        
        view.setOnClickListener(v -> {
            dbHelper.recordView(video);
            Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
            intent.putExtra("path", video.path);
            intent.putExtra("title", video.title);
            startActivity(intent);
        });

        container.addView(view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadVideos();
        } else {
            Toast.makeText(this, R.string.permissions_required, Toast.LENGTH_LONG).show();
        }
    }
}
