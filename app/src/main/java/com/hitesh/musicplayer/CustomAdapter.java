package com.hitesh.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.hitesh.musicplayer.MainActivity.mainActivity;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomVH> {
    private List<MainActivity.Song> musicList = new ArrayList<>();
    private Context context;
    public static MediaPlayer mediaPlayer;
    public static Integer currentPlaying = -1;
    private boolean setPlayerCompletionListener; //false default
    public Thread progressThread;
    public boolean breakThread;
    public static int i; //looper for thread of progress bar
    public static boolean seekBarTouched = false;
    public static Intent changeName;

    public CustomAdapter(Context context, List<MainActivity.Song> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_holder, parent, false);
        return new CustomVH(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final CustomVH holder, final int position) {
        String name = musicList.get(position).title;
        holder.name.setText(name);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {
        TextView name;
        ImageView logo;
        LinearLayout song;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            logo = (ImageView) itemView.findViewById(R.id.logo);
            song = (LinearLayout) itemView.findViewById(R.id.song);
            song.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    play(getAdapterPosition());
                }
            });
        }
    }

    private void setPlayerCompletionListener() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext();
            }
        });
    }

    public void playNext() {
        if (!mediaPlayer.isPlaying())
            mediaPlayer.start();
        if ((currentPlaying + 1) < musicList.size()) {
            play(currentPlaying + 1);
        } else if (currentPlaying + 1 == musicList.size()) {
            play(0);
        }
    }

    public void playPrevious() {
        if (musicList.size() > 0) {
            Log.i("Music", "Going back");
            if (!mediaPlayer.isPlaying())
                mediaPlayer.start();
            if ((currentPlaying - 1) < 0)
                play(musicList.size() - 1);
            else
                play(currentPlaying - 1);
        }
    }

    public void play(final int position) {
        mainActivity.pause.setImageResource(R.drawable.pause);
        breakThread = true;
        currentPlaying = position;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(context, musicList.get(position).uri);
        mediaPlayer.start();
        if (!setPlayerCompletionListener) {
            setPlayerCompletionListener();
        }
        final int length = mediaPlayer.getDuration() / 100;
        mainActivity.progress.setMax(length);
        mainActivity.progress.setProgress(0);
        progressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                breakThread = false;
                for (i = 1; i <= length && !breakThread; ++i) {
                    if (breakThread)
                        return;
                    SystemClock.sleep(100);
                    if (breakThread)
                        return;
                    while (!mediaPlayer.isPlaying() || seekBarTouched) {
                        if (breakThread)
                            return;
                    }
                    if (breakThread)
                        return;
                    mainActivity.progress.setProgress(i);
                    if (breakThread)
                        return;
                }
            }
        });
        progressThread.start();
        String name = musicList.get(position).title;
        mainActivity.name.setText(name);
        changeName = new Intent(context, BRService.class);
        changeName.setAction(MainActivity.ACTION_UPDATE_NAME);
        changeName.putExtra(MainActivity.NAME_TAG, name);
        context.sendBroadcast(changeName);
        mainActivity.totalTime.setText(convertMillis(mediaPlayer.getDuration()));
        mainActivity.name.setSelected(true);
    }

    public static String convertMillis(long ms) {
        long secTotal = ms / 1000;
        long min = secTotal / 60;
        long sec = secTotal % 60;
        if (sec > 9)
            return (min + ":" + sec);
        return  (min + ": 0" + sec);
    }
}
