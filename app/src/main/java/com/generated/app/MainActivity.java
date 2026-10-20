package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
    private TextView labelRec;
    private List<VideoModel> allVideos = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        videoContainer = findViewById(R.id.video_container);
        recommendationContainer = findViewById(R.id.recommendation_container);
        labelRec = findViewById(R.id.label_rec);
        ImageButton btnSettings = findViewById(R.id.btn_settings);

        btnSettings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, 101);
            } else {
                loadVideos();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            } else {
                loadVideos();
            }
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
            int idCol = cursor.getColumnIndex(MediaStore.Video.Media._ID);
            int titleCol = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
            int dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            int durCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
            int sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);

            do {
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);
                String path = cursor.getString(dataCol);
                long durationMs = cursor.getLong(durCol);
                long size = cursor.getLong(sizeCol);
                
                String folder = "Video";
                File file = new File(path);
                if (file.getParentFile() != null) folder = file.getParentFile().getName();

                String duration = formatDuration(durationMs);
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

    private String formatDuration(long ms) {
        return String.format("%02d:%02d", 
                TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
    }

    private void buildRecommendations() {
        List<Long> recommendedIds = dbHelper.getRecommendedIds();
        boolean hasRec = false;
        for (Long id : recommendedIds) {
            for (VideoModel v : allVideos) {
                if (v.id == id) {
                    addVideoToLayout(v, recommendationContainer);
                    hasRec = true;
                    break;
                }
            }
        }
        labelRec.setVisibility(hasRec ? View.VISIBLE : View.GONE);
    }

    private void addVideoToLayout(final VideoModel video, LinearLayout container) {
        View view = LayoutInflater.from(this).inflate(R.layout.video_item, container, false);
        TextView title = view.findViewById(R.id.video_title);
        TextView info = view.findViewById(R.id.video_info);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (!allVideos.isEmpty()) buildRecommendations();
    }
}
