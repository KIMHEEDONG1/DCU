package com.example.dcu1;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SubActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private TextView currentTimeTextView, songTitleTextView;
    private int songDuration;
    private Thread playbackThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        ImageButton backBtn = findViewById(R.id.back_btn);
        seekBar = findViewById(R.id.seekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        songTitleTextView = findViewById(R.id.songTitleTextView);

        Intent intent = getIntent();
        String selectedSong = intent.getStringExtra("selected_song");
        String songTitle = intent.getStringExtra("song_title");
        songDuration = intent.getIntExtra("song_duration", 0);

        songTitleTextView.setText(songTitle);

        backBtn.setOnClickListener(v -> {
            stopAndReleaseMediaPlayer();
            finish();
        });

        int resId = getResources().getIdentifier(selectedSong, "raw", getPackageName());
        mediaPlayer = MediaPlayer.create(SubActivity.this, resId);
        mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(songDuration);
            currentTimeTextView.setText(formatDuration(songDuration));
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        });
        mediaPlayer.start();
        startPlaybackThread();
    }

    private String formatDuration(int duration) {
        int minutes = duration / 1000 / 60;
        int seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void startPlaybackThread() {
        playbackThread = new Thread(() -> {
            while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    Thread.sleep(1000);
                    runOnUiThread(() -> {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int currentPosition = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPosition);
                            currentTimeTextView.setText(formatDuration(currentPosition));
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        playbackThread.start();
    }

    private void stopAndReleaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
            playbackThread = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndReleaseMediaPlayer();
    }
}
