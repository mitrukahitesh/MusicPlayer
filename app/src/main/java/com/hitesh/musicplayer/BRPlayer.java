package com.hitesh.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;

import static com.hitesh.musicplayer.MainActivity.mainActivity;

public class BRPlayer extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.ACTION_PREVIOUS.equals(intent.getAction())) {
            mainActivity.onPreviousClicked();
        } else if (MainActivity.ACTION_PAUSE.equals(intent.getAction())) {
            mainActivity.onPauseClick();
        } else if (MainActivity.ACTION_NEXT.equals(intent.getAction())) {
            mainActivity.onNextClicked();
        } else if (MainActivity.ACTION_FORWARD.equals(intent.getAction())) {
            mainActivity.onForward();
        } else if (MainActivity.ACTION_REPLAY.equals(intent.getAction())) {
            mainActivity.onReplay();
        }
    }
}
