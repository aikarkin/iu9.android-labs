package iu9.bmstu.ru.lab10;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.function.BiConsumer;

@RequiresApi(api = Build.VERSION_CODES.P)
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final int PICK_AUDIO_REQ_CODE = 1;
    private static final int REQUIRE_PERM_REQ_CODE = 2;

    private static final String[] PERMS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE
    };

    private static String selectedTrackPath;
    private static String selectedTrackName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (String perm : PERMS) {
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMS, REQUIRE_PERM_REQ_CODE);
                break;
            }
        }

        final Button btnFChoose = findViewById(R.id.btnChooseFile);
        final Button btnSetAlarm = findViewById(R.id.btnSetAlarm);
        final EditText editTxtFile = findViewById(R.id.editTxtFile);
        final EditText editTxtTime = findViewById(R.id.editTxtTime);

        BiConsumer<View, String> onEditChanged = (view, str) -> {
            String time = editTxtTime.getText().toString();
            String fileName = editTxtFile.getText().toString();

            if (!fileName.isEmpty() && time.matches("\\d{1,2}:\\d{1,2}(:\\d{1,2})?")) {
                btnSetAlarm.setEnabled(true);
            } else {
                btnSetAlarm.setEnabled(false);
            }
        };

        editTxtFile.addTextChangedListener(new OnTextChangedWatcher(editTxtFile, onEditChanged));
        editTxtTime.addTextChangedListener(new OnTextChangedWatcher(editTxtTime, onEditChanged));

        btnFChoose.setOnClickListener(btnView -> {
            Intent pickFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickFileIntent.setType("audio/*");
            startActivityForResult(pickFileIntent, PICK_AUDIO_REQ_CODE);
        });
        btnSetAlarm.setOnClickListener(btnView -> startPlayerDelayed());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUIRE_PERM_REQ_CODE) {

            if (grantResults.length > 0) {
                for (int grantRes : grantResults) {
                    if (grantRes == PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "onRequestPermissionsResult: Permission granted");
                    } else {
                        Log.w(TAG, "onRequestPermissionsResult: Permission was not granted: " + grantRes);
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PICK_AUDIO_REQ_CODE: {
                EditText editTxtFile = findViewById(R.id.editTxtFile);
                if (data != null && data.getData() != null) {
                    Uri selectedTrackUri = data.getData();
                    selectedTrackPath = selectedTrackUri.toString();
                    Log.i(TAG, "onActivityResult: picked file path: " + selectedTrackPath);
                    String trackName = getTrackName(selectedTrackUri);
                    if (trackName != null) {
                        selectedTrackName = trackName;
                    } else {
                        selectedTrackName = selectedTrackUri.getLastPathSegment();
                    }
                    editTxtFile.setText(selectedTrackName);
                } else {
                    Log.w(TAG, "onActivityResult: no file picked, nothing happened");
                }
                break;
            }
            default:
                Log.w(TAG, "onActivityResult: Unknown activity request code");
                break;
        }
    }

    private String getTrackName(Uri fileUri) {
        String trackName = null;
        String trackFilePath = fileUri.getPath();
//        Cursor fileCursor = getContentResolver().query(fileUri, null, null, null, null);
        if (trackFilePath != null) {
            Log.i(TAG, "getTrackName: req file path: " + new File(trackFilePath).getAbsolutePath());
            Cursor trackCursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.TRACK,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.YEAR
                    },
                    MediaStore.Audio.Media.DATA + " = ?",
                    new String[]{
                            new File(trackFilePath).getAbsolutePath()
                    },
                    "");

            if (trackCursor != null) {
                trackCursor.moveToFirst();
                if (trackCursor.getCount() > 0) {
                    trackName = trackCursor.getString(trackCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    trackCursor.close();
                } else {
                    Log.i(TAG, "getTrackName: query cursor is empty");
                }
            }

        }
        return trackName;
    }

    private void startPlayerDelayed() {
        String[] time = ((EditText) findViewById(R.id.editTxtTime)).getText().toString().split(":");
        int specHours = Integer.valueOf(time[0]), specMins = Integer.valueOf(time[1]);

        if (specHours < 0 || specHours > 24 || specMins < 0 || specMins > 59) {
            Log.e(TAG, "startPlayerDelayed: invalid time format");
            Toast
                    .makeText(getApplicationContext(), "Invalid time format. Valid format is 'hh:mm', where 'hh' - hours between 0 to 23, 'mm' - minutes beetween 0 to 59.", Toast.LENGTH_LONG)
                    .show();
            ((EditText) findViewById(R.id.editTxtTime)).setText("");
            findViewById(R.id.btnSetAlarm).setEnabled(false);
            return;
        }

        AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        if (manager == null) {
            Toast
                    .makeText(getApplicationContext(), "It is not possible to start alarm. AlarmManager is not works. Sorry.", Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "startPlayerDelayed: failed to get AlarmManager. No action set at specified time");
            return;
        }

        Calendar specifiedTime = Calendar.getInstance();
        int curHours, curMinutes, curDay;
        curDay = specifiedTime.get(Calendar.DAY_OF_YEAR);
        curHours = specifiedTime.get(Calendar.HOUR_OF_DAY);
        curMinutes = specifiedTime.get(Calendar.MINUTE);

        if (curHours >= specHours && curMinutes >= specMins) {
            specifiedTime.set(Calendar.DAY_OF_YEAR, (curDay + 1) % 365);
        }

        specifiedTime.set(Calendar.HOUR_OF_DAY, specHours);
        specifiedTime.set(Calendar.MINUTE, specMins);
        specifiedTime.set(Calendar.SECOND, 0);

        Intent broadcastIntent = new Intent(getApplicationContext(), PlayerBroadcastReceiver.class);
        broadcastIntent.putExtra("trackPath", selectedTrackPath);
        broadcastIntent.putExtra("trackName", selectedTrackName);
        broadcastIntent.setAction(Const.Action.START_SVC);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, broadcastIntent, 0);
        manager.set(AlarmManager.RTC_WAKEUP, specifiedTime.getTimeInMillis(), pendingIntent);
        Toast.makeText(this, String.format("Alarm set at %s:%s", time[0], time[1]), Toast.LENGTH_SHORT).show();
    }

    private static class OnTextChangedWatcher implements TextWatcher {
        private BiConsumer<View, String> consumer;
        private View view;

        OnTextChangedWatcher(View view, BiConsumer<View, String> consumer) {
            this.consumer = consumer;
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            consumer.accept(view, charSequence.toString());
        }
    }
}
