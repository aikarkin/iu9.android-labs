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

        if(action != null) {
            Log.i(TAG, "onReceive: action: " + action);
            Intent svcStartIntent = new Intent(context, PlayerForegroundService.class);
            svcStartIntent.putExtra("trackName", intent.getStringExtra("trackName"));
            svcStartIntent.putExtra("trackPath", intent.getStringExtra("trackPath"));

            switch (action) {
                case Const.Action.START_SVC:
                    svcStartIntent.setAction(Const.Action.START_SVC);
                    break;
                case Const.Action.PLAY:
                    svcStartIntent.setAction(Const.Action.PLAY);
//                    PlayerForegroundService.showNotificationPlayer(context, trackName, true);
                    break;
                case Const.Action.STOP:
                    svcStartIntent.setAction(Const.Action.STOP);
//                    PlayerForegroundService.showNotificationPlayer(context, trackName, false);
                    break;
            }
            context.startForegroundService(svcStartIntent);
            Log.i(TAG, "onReceive: service must be started");
        }
    }
}
