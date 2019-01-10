package iu9.bmstu.ru.lab10;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

import static iu9.bmstu.ru.lab10.Const.NOTIFICATION_PLAYER_CHANNEL_ID;

public class PlayerForegroundService extends Service {
    private static final String TAG = "PlayerForegroundService";

    private MediaPlayer mPlayer;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null) {
            Log.i(TAG, "onStartCommand: current action: " + action);
            String filePath = intent.getStringExtra("trackPath");
            String trackName = intent.getStringExtra("trackName");
            switch (action) {
                case Const.Action.START_SVC: {
                    Calendar curTime = Calendar.getInstance();

                    Log.i(TAG, "onStartCommand: service started! Wake up!!!");
                    Log.i(TAG, String.format("onStartCommand: current time - %d:%d", curTime.get(Calendar.HOUR_OF_DAY), curTime.get(Calendar.MINUTE)));
                    Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
                    showNotificationPlayer(this, trackName, true);

                    if (filePath != null) {
                        try {
                            mPlayer.setDataSource(filePath);
                            mPlayer.prepare();
                            mPlayer.setOnPreparedListener(MediaPlayer::start);
                            Log.i(TAG, "onStartCommand: player started");
                        } catch (IOException e) {
                            Log.e(TAG, "onStartCommand: failed to set data source", e);
                        }
                    }
                    break;
                }
                case Const.Action.PLAYBACK_START:
                    mPlayer.start();
                    if (trackName != null) {
                        showNotificationPlayer(this, trackName, true);
                    }
                    break;
                case Const.Action.PLAYBACK_STOP:
                    if (trackName != null) {
                        showNotificationPlayer(this, trackName, false);
                    }
                    mPlayer.pause();
                    mPlayer.seekTo(0);
                    break;
                case Const.Action.STOP_SVC:
                    stopForeground(true);
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(mPlayer != null) {
            mPlayer.release();
        }
        super.onDestroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void showNotificationPlayer(Context ctx, String trackName, boolean isPlaying) {
        int actionBtnDrawable;
        String btnLabel, action;
        Log.i(TAG, "showNotificationPlayer: ");


        if (isPlaying) {
            actionBtnDrawable = R.drawable.ic_stop_white_24dp;
            btnLabel = "Stop";
            action = Const.Action.PLAYBACK_STOP;
        } else {
            actionBtnDrawable = R.drawable.ic_play_arrow_white_24dp;
            btnLabel = "Play";
            action = Const.Action.PLAYBACK_START;
        }

        Intent playerActionIntent = new Intent(ctx, PlayerBroadcastReceiver.class);
        playerActionIntent.setAction(action);
        playerActionIntent.putExtra("trackName", trackName);
        PendingIntent pendingPlayerActionIntent = PendingIntent.getBroadcast(ctx, 1, playerActionIntent, 0);

        Intent serviceStopIntent = new Intent(this, BroadcastReceiver.class);
        serviceStopIntent.setAction(Const.Action.STOP_SVC);
        PendingIntent pendingSvcStop = PendingIntent.getBroadcast(ctx, 2, serviceStopIntent, 0);

        Notification notification = new NotificationCompat.Builder(ctx, NOTIFICATION_PLAYER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                .setContentTitle("Wake Up!")
                .setContentText(trackName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(actionBtnDrawable, btnLabel, pendingPlayerActionIntent)
                .setDeleteIntent(pendingSvcStop)
                .build();

        NotificationManager mNotifyManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotifyManager != null) {
            Log.i(TAG, "showNotificationPlayer: NotificationManager service works!");
            String name = "PlayingMusic";
            String description = "Notifications for control music playing";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_PLAYER_CHANNEL_ID, name, importance);
                mChannel.setDescription(description);
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.BLUE);
                mNotifyManager.createNotificationChannel(mChannel);
                Log.i(TAG, "showNotificationPlayer: notification channel set");
            }

//            mNotifyManager.notify(Const.PLAYER_NOTIFICATION_ID, notification);
            this.startForeground(Const.PLAYER_NOTIFICATION_ID, notification);
        }
        Log.i(TAG, "showNotificationPlayer: notification must be shown");
    }
}
