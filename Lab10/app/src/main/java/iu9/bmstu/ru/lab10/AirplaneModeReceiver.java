package iu9.bmstu.ru.lab10;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AirplaneModeReceiver extends BroadcastReceiver {
    private static final String TAG = "AirplaneModeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: ");
        String action = intent.getAction();
        if(action != null && action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED) && isAirplaneModeOn(context)) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Const.AIRPLANE_MODE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setOngoing(false)
                    .setContentTitle("!!WARNING!!")
                    .setContentText("Alarm could work incorrectly in airplane mode");
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(Const.AIRPLANE_MODE_CHANNEL_ID, Const.AIRPLANE_MODE_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
                    mChannel.enableLights(true);
                    mChannel.setLightColor(Color.BLUE);
                    manager.createNotificationChannel(mChannel);
                    Log.i(TAG, "onReceive: notification channel set");
                }
                manager.notify(Const.AIRPLANE_MODE_NOTIFICATION_ID, mBuilder.build());
            } else {
                Log.e(TAG, "onReceive: failed to get notification manager");
            }
        }
    }

    private static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;

    }
}
