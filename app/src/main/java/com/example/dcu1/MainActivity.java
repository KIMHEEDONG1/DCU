package com.example.dcu1;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ListView listViewMP3;
    private String[] mp3Files = {"song1", "song2", "song3", "song4", "song5"};
    private String[] songTitles = {"Song 1", "Song 2", "Song 3", "Song 4", "Song 5"};
    private int[] images = {R.drawable.not};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewMP3 = findViewById(R.id.listViewMP3);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listview_image, R.id.songName, songTitles) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView imageView = view.findViewById(R.id.mp3not);
                imageView.setImageResource(images[0]);
                view.setBackgroundColor(Color.TRANSPARENT);

                TextView durationTextView = view.findViewById(R.id.songDuration);
                int resId = getResources().getIdentifier(mp3Files[position], "raw", getPackageName());
                MediaPlayer tempMediaPlayer = MediaPlayer.create(MainActivity.this, resId);
                int duration = tempMediaPlayer.getDuration();
                durationTextView.setText(formatDuration(duration));
                tempMediaPlayer.release();

                return view;
            }
        };
        listViewMP3.setAdapter(adapter);

        listViewMP3.setOnItemClickListener((parent, view, position, id) -> {
            int resId = getResources().getIdentifier(mp3Files[position], "raw", getPackageName());
            MediaPlayer tempMediaPlayer = MediaPlayer.create(MainActivity.this, resId);
            int duration = tempMediaPlayer.getDuration();
            tempMediaPlayer.release();

            Intent intent = new Intent(MainActivity.this, SubActivity.class);
            intent.putExtra("selected_song", mp3Files[position]);
            intent.putExtra("song_title", songTitles[position]);
            intent.putExtra("song_duration", duration);
            startActivity(intent);
        });
    }

    private String formatDuration(int duration) {
        int minutes = duration / 1000 / 60;
        int seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
