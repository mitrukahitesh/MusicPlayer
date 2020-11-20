package com.hitesh.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static final int REQUEST_PER_CODE = 1;
    private RecyclerView recyclerView;
    private List<Song> musicList = new ArrayList<>();
    private ImageView previous, next, forward, replay;
    public static final String ACTION_UPDATE_NAME = "com.hitesh.musicplayer.UPDATE_NAME";
    public static final String ACTION_UPDATE_PAUSE = "com.hitesh.musicplayer.UPDATE_PAUSE";
    public static final String ACTION_PREVIOUS = "com.hitesh.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_PAUSE = "com.hitesh.musicplayer.ACTION_PAUSE";
    public static final String ACTION_NEXT = "com.hitesh.musicplayer.ACTION_NEXT";
    public static final String ACTION_FORWARD = "com.hitesh.musicplayer.ACTION_FORWARD";
    public static final String ACTION_REPLAY = "com.hitesh.musicplayer.ACTION_REPLAY";
    public static final String NAME_TAG = "name";
    ImageView pause;
    TextView name, totalTime, currentTime;
    SeekBar progress;
    static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askPermission();
        setMusicList();
        setReferences();
        setListeners();
        mainActivity = this;
    }

    private void setReferences() {
        pause = (ImageView) findViewById(R.id.pause);
        previous = (ImageView) findViewById(R.id.previous);
        next = (ImageView) findViewById(R.id.next);
        forward = (ImageView) findViewById(R.id.forward);
        replay = (ImageView) findViewById(R.id.replay);
        name = (TextView) findViewById(R.id.playing);
        totalTime = (TextView) findViewById(R.id.totalTime);
        currentTime = (TextView) findViewById(R.id.currentTime);
        progress = (SeekBar) findViewById(R.id.progress);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        CustomAdapter customAdapter = new CustomAdapter(this, musicList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(customAdapter);
    }


    private void setListeners() {
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseClick();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPreviousClicked();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClicked();
            }
        });

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTime.setText(CustomAdapter.convertMillis(progress * 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                CustomAdapter.seekBarTouched = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (CustomAdapter.seekBarTouched && CustomAdapter.mediaPlayer.isPlaying()) {
                    CustomAdapter.mediaPlayer.seekTo(progress.getProgress() * 100);
                    CustomAdapter.i = CustomAdapter.mediaPlayer.getCurrentPosition() / 100;
                }
                CustomAdapter.seekBarTouched = false;
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onForward();
            }
        });

        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReplay();
            }
        });

    }

    public void onReplay() {
        if (CustomAdapter.i <= 100)
            CustomAdapter.i = 0;
        else
            CustomAdapter.i -= 100;
        CustomAdapter.mediaPlayer.seekTo(CustomAdapter.mediaPlayer.getCurrentPosition() - 10000);
    }

    public void onForward() {
        CustomAdapter.i += 100;
        CustomAdapter.mediaPlayer.seekTo(CustomAdapter.mediaPlayer.getCurrentPosition() + 10000);
    }

    public void onPauseClick() {
        if (CustomAdapter.mediaPlayer == null) {
            ((CustomAdapter) recyclerView.getAdapter()).play(0);
        } else {
            if (CustomAdapter.mediaPlayer.isPlaying()) {
                CustomAdapter.mediaPlayer.pause();
                pause.setImageResource(R.drawable.play);
            } else {
                CustomAdapter.mediaPlayer.start();
                CustomAdapter.mediaPlayer.seekTo(progress.getProgress() * 100);
                CustomAdapter.i = CustomAdapter.mediaPlayer.getCurrentPosition() / 100;
                pause.setImageResource(R.drawable.pause);
            }
            Intent broadcastIntent = new Intent(ACTION_UPDATE_PAUSE);
            broadcastIntent.setPackage("com.hitesh.musicplayer");
            sendBroadcast(broadcastIntent);
        }
    }

    public void onPreviousClicked() {
        if (CustomAdapter.mediaPlayer == null) {
            ((CustomAdapter) recyclerView.getAdapter()).play(0);
            return;
        }
        if (!CustomAdapter.mediaPlayer.isPlaying()) {
            progress.setProgress(0);
            return;
        }
        if (CustomAdapter.mediaPlayer.getCurrentPosition() > 5000) {
            CustomAdapter.mediaPlayer.seekTo(0);
            CustomAdapter.i = 0;
        } else {
            ((CustomAdapter) recyclerView.getAdapter()).playPrevious();
        }
    }

    public void onNextClicked() {
        if (CustomAdapter.mediaPlayer == null) {
            ((CustomAdapter) recyclerView.getAdapter()).play(0);
        } else {
            ((CustomAdapter) recyclerView.getAdapter()).playNext();
        }
    }

    private void setMusicList() {

        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(
                songUri,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE},
                null, null, MediaStore.Audio.Media.TITLE);
        if (songCursor != null && songCursor.moveToFirst()) {
            int id = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int title = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            do {
                Uri thisSongIsAt = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songCursor.getLong(id));
                musicList.add(new Song(songCursor.getString(title), thisSongIsAt));
            } while (songCursor.moveToNext());
        }

        /*File musicDir = new File(dirPath);
        if (musicDir.exists()) {
            List<File> files = new ArrayList<>();
            List<String> folders = new ArrayList<>();
            files = Arrays.asList(musicDir.listFiles());
            for (int i = 0; i < files.size(); ++i) {
                File file = files.get(i);
                if (file.isDirectory()) {
                    folders.add(file.getAbsolutePath() + "/");
                } else {
                    String name = file.getAbsolutePath();
                    if (name.endsWith(".mp3") || name.endsWith(".mp4") || name.endsWith(".aac") || name.endsWith(".amr")) {
                        musicList.add(name);
                    }
                }
            }
            if (folders.size() != 0) {
                for (String path : folders) {
                    setMusicList(path);
                }
            }
        }*/
    }

    public void resetMusicList() {
        musicList.clear();
        setMusicList();
        CustomAdapter customAdapter = new CustomAdapter(this, musicList);
        recyclerView.setAdapter(customAdapter);
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, REQUEST_PER_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PER_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetMusicList();
    }

    @Override
    protected void onDestroy() {
        ControllerService.stopService();
        ((CustomAdapter) recyclerView.getAdapter()).breakThread = true;
        if (CustomAdapter.mediaPlayer != null) {
            CustomAdapter.mediaPlayer.release();
            CustomAdapter.mediaPlayer = null;
        }
        super.onDestroy();
    }

    public class Song {
        public String title;
        public Uri uri;

        public Song(String title, Uri uri) {
            this.title = title;
            this.uri = uri;
        }
    }
}