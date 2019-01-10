package iu9.bmstu.ru.lab10;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlayerBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "PlayerBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: ");
        String action = intent.getAction();

        if (action != null) {
            Log.i(TAG, "onReceive: action: " + action);
            Intent svcStartIntent = new Intent(context, PlayerForegroundService.class);
            svcStartIntent.putExtra("trackName", intent.getStringExtra("trackName"));
            svcStartIntent.putExtra("trackPath", intent.getStringExtra("trackPath"));

            switch (action) {
                case Const.Action.START_SVC:
                    svcStartIntent.setAction(Const.Action.START_SVC);
                    break;
                case Const.Action.PLAYBACK_START:
                    svcStartIntent.setAction(Const.Action.PLAYBACK_START);
                    break;
                case Const.Action.PLAYBACK_STOP:
                    svcStartIntent.setAction(Const.Action.PLAYBACK_STOP);
                    break;
                case Const.Action.STOP_SVC:
                    Log.i(TAG, "onReceive: stopping service ...");
                    svcStartIntent.setAction(Const.Action.STOP_SVC);
                    return;
            }
            Log.i(TAG, "onReceive: service must be started");
            context.startForegroundService(svcStartIntent);
        }
    }

}