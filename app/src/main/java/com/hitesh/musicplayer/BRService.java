package com.hitesh.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class BRService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.ACTION_UPDATE_NAME.equals(intent.getAction())){
            Intent serviceIntent = new Intent(context, ControllerService.class);
            serviceIntent.putExtra(MainActivity.NAME_TAG, intent.getStringExtra(MainActivity.NAME_TAG));
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
