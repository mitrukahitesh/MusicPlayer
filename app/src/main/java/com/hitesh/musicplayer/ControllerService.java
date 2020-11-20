package com.hitesh.musicplayer;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ControllerService extends Service {
    public static final Integer NOTIFICATION_ID = 1;
    public static final String TAG = "Music";
    private static Notification notification;
    private static String name;
    private static NotificationCompat.Builder builder;
    public static ControllerService controllerService;
    private static PendingIntent previousPending;
    private static PendingIntent pausePending;
    private static PendingIntent nextPending;
    private static PendingIntent activityPending;
    private static PendingIntent forwardPending;
    private static PendingIntent replayPending;
    private MediaSession mediaSession;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        controllerService = this;
        mediaSession = new MediaSession(getApplicationContext(), TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        name = intent.getStringExtra(MainActivity.NAME_TAG);
        setIntents();
        setNotification();
        startForeground(NOTIFICATION_ID, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    private void setIntents() {
        Intent previous = new Intent(getApplicationContext(), BRPlayer.class);
        previous.setAction(MainActivity.ACTION_PREVIOUS);
        previousPending = PendingIntent.getBroadcast(getApplicationContext(), 1, previous, 0);

        Intent pause = new Intent(getApplicationContext(), BRPlayer.class);
        pause.setAction(MainActivity.ACTION_PAUSE);
        pausePending = PendingIntent.getBroadcast(getApplicationContext(), 2, pause, 0);

        Intent next = new Intent(getApplicationContext(), BRPlayer.class);
        next.setAction(MainActivity.ACTION_NEXT);
        nextPending = PendingIntent.getBroadcast(getApplicationContext(), 3, next, 0);

        Intent forward = new Intent(getApplicationContext(), BRPlayer.class);
        forward.setAction(MainActivity.ACTION_FORWARD);
        forwardPending = PendingIntent.getBroadcast(getApplicationContext(), 4, forward, 0);

        Intent replay = new Intent(getApplicationContext(), BRPlayer.class);
        replay.setAction(MainActivity.ACTION_REPLAY);
        replayPending = PendingIntent.getBroadcast(getApplicationContext(), 4, replay, 0);

        Intent activity = new Intent(this, MainActivity.class);
        activityPending = PendingIntent.getActivity(getApplicationContext(), 6, activity, 0);
    }

    private void setNotification() {
        Bitmap imageLarge = BitmapFactory.decodeResource(getResources(), R.raw.imagelarge);
        builder = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setSmallIcon(R.drawable.music)
                .setLargeIcon(imageLarge)
                .setContentTitle(name)
                .setColor(Color.BLACK)
                .setSound(null)
                .setVibrate(null)
                .addAction(R.drawable.ic_baseline_replay_10_24, null, replayPending)
                .addAction(R.drawable.previous, null, previousPending)
                .addAction(R.drawable.pause, null, pausePending)
                .addAction(R.drawable.next, null, nextPending)
                .addAction(R.drawable.ic_baseline_forward_10_24, null, forwardPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1, 2, 3)
                        .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getSessionToken())))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(activityPending);
        notification = builder.build();
    }

    public static class PauseUpdater extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bitmap imageLarge = BitmapFactory.decodeResource(context.getResources(), R.raw.imagelarge);
            Notification.Action action;
            int icon;
            if (!CustomAdapter.mediaPlayer.isPlaying()) {
                icon = R.drawable.play;
                action = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.play), null, pausePending).build();
            } else {
                icon = R.drawable.pause;
                action = new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.pause), null, pausePending).build();
            }
            notification.actions[2] = action;
            if (Build.VERSION.SDK_INT <= 23) {
                notification = new NotificationCompat.Builder(context, App.CHANNEL_ID)
                        .setSmallIcon(R.drawable.music)
                        .setLargeIcon(imageLarge)
                        .setContentTitle(name)
                        .setColor(Color.BLACK)
                        .setSound(null)
                        .setVibrate(null)
                        .addAction(R.drawable.ic_baseline_replay_10_24, null, replayPending)
                        .addAction(R.drawable.previous, null, previousPending)
                        .addAction(icon, null, pausePending)
                        .addAction(R.drawable.next, null, nextPending)
                        .addAction(R.drawable.ic_baseline_forward_10_24, null, forwardPending)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(1, 2, 3))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setContentIntent(activityPending)
                        .build();
            }
            controllerService.startForeground(1, notification);
        }
    }

    public static void stopService() {
        controllerService.stopSelf();
    }
}
