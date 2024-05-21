package com.example.dcu;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    ListView listViewMP3;
    int currentPlayingPosition = -1;
    MediaPlayer mediaPlayer;
    String[] mp3Files = {"song1", "song2", "song3", "song4", "song5"};
    int[] images = {R.drawable.not, R.drawable.play};
    boolean[] isPlaying = new boolean[mp3Files.length];
    SeekBar currentSeekBar = null;
    Thread playbackThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewMP3 = findViewById(R.id.listViewMP3);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listview_image, R.id.songName, mp3Files) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView imageView = view.findViewById(R.id.mp3not);
                imageView.setImageResource(currentPlayingPosition == position ? images[1] : images[0]);
                if (currentPlayingPosition == position) {
                    view.setBackgroundColor(Color.parseColor("#FFD700")); // Set background color to #FFD700 if the item is playing
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT); // Set transparent background color for other items
                }
                return view;
            }
        };
        listViewMP3.setAdapter(adapter);

        listViewMP3.setOnItemClickListener((parent, view, position, id) -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                if (currentPlayingPosition != position) {
                    isPlaying[currentPlayingPosition] = false;
                    if (currentSeekBar != null) {
                        currentSeekBar.setVisibility(View.GONE);
                    }
                    if (playbackThread != null) {
                        playbackThread.interrupt();
                        playbackThread = null;
                    }
                }
            }

            @SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(mp3Files[position], "raw", getPackageName());
            mediaPlayer = MediaPlayer.create(MainActivity.this, resId);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                SeekBar seekBar = view.findViewById(R.id.seekBar);
                TextView currentTimeTextView = view.findViewById(R.id.currentTimeTextView);
                seekBar.setVisibility(View.VISIBLE);
                seekBar.setMax(mp.getDuration());
                currentSeekBar = seekBar;

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

                playbackThread = new Thread(() -> {
                    while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        try {
                            Thread.sleep(1000);
                            runOnUiThread(() -> {
                                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                    int currentPosition = mediaPlayer.getCurrentPosition();
                                    currentSeekBar.setProgress(currentPosition);
                                    int minutes = currentPosition / 1000 / 60;
                                    int seconds = (currentPosition / 1000) % 60;
                                    String currentTime = String.format("%02d:%02d", minutes, seconds);
                                    currentTimeTextView.setText(currentTime);
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                playbackThread.start();
            });

            currentPlayingPosition = position;
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
            playbackThread = null;
        }
    }
}