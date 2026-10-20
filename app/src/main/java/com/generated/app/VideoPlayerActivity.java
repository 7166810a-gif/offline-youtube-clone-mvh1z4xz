package com.generated.app;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Rational;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayerActivity extends Activity {

    private VideoView videoView;
    private RelativeLayout controls;
    private ImageButton btnPlay;
    private SeekBar seekBar;
    private TextView tvTime, tvSubtitles;
    private ProgressBar aiProgress;
    private Handler handler = new Handler();
    private boolean isCcEnabled = false;
    private final String[] subSamples = {
        "זיהוי קולי מקומי פעיל...",
        "מתרגם טקסט ללא אינטרנט...",
        "ספרדי גאה - טכנולוגיית AI",
        "מנגנון כתוביות אופליין",
        "המשך צפייה מהנה!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        String path = getIntent().getStringExtra("path");

        videoView = findViewById(R.id.video_view);
        controls = findViewById(R.id.controls_overlay);
        btnPlay = findViewById(R.id.btn_play_pause);
        seekBar = findViewById(R.id.seek_bar);
        tvTime = findViewById(R.id.video_time);
        tvSubtitles = findViewById(R.id.subtitle_view);
        aiProgress = findViewById(R.id.ai_processing);
        
        findViewById(R.id.btn_back_player).setOnClickListener(v -> finish());

        videoView.setVideoPath(path);
        videoView.setOnPreparedListener(mp -> {
            seekBar.setMax(videoView.getDuration());
            updateTime();
            videoView.start();
        });

        btnPlay.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            } else {
                videoView.start();
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) videoView.seekTo(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.btn_pip).setOnClickListener(v -> enterPip());
        
        findViewById(R.id.btn_cc).setOnClickListener(v -> {
            if (!isCcEnabled) {
                simulateAiSubtitleGeneration();
            } else {
                isCcEnabled = false;
                tvSubtitles.setVisibility(View.GONE);
            }
        });

        videoView.setOnClickListener(v -> {
            if (controls.getVisibility() == View.VISIBLE) {
                controls.setVisibility(View.GONE);
            } else {
                controls.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> {
                    if (videoView.isPlaying()) controls.setVisibility(View.GONE);
                }, 3000);
            }
        });
    }

    private void simulateAiSubtitleGeneration() {
        aiProgress.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            aiProgress.setVisibility(View.GONE);
            isCcEnabled = true;
            tvSubtitles.setVisibility(View.VISIBLE);
            tvSubtitles.setText(getString(R.string.loading_ai));
            startSubtitleLoop();
        }, 1500);
    }

    private void startSubtitleLoop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isCcEnabled && videoView.isPlaying()) {
                    int sec = videoView.getCurrentPosition() / 2000;
                    tvSubtitles.setText(subSamples[sec % subSamples.length]);
                }
                if (isCcEnabled) handler.postDelayed(this, 2000);
            }
        });
    }

    private void updateTime() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (videoView != null) {
                    int curr = videoView.getCurrentPosition();
                    int total = videoView.getDuration();
                    seekBar.setProgress(curr);
                    tvTime.setText(formatTime(curr) + " / " + formatTime(total));
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private String formatTime(int ms) {
        int s = ms / 1000;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    private void enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Display d = getWindowManager().getDefaultDisplay();
            Point p = new Point();
            d.getSize(p);
            Rational ratio = new Rational(p.x, p.y);
            try {
                PictureInPictureParams params = new PictureInPictureParams.Builder()
                        .setAspectRatio(ratio).build();
                enterPictureInPictureMode(params);
            } catch (Exception e) {
                // Ignore if PiP not supported on specific device configuration
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            controls.setVisibility(View.GONE);
            tvSubtitles.setTextSize(14sp);
        } else {
            controls.setVisibility(View.VISIBLE);
            tvSubtitles.setTextSize(20sp);
        }
    }
}
