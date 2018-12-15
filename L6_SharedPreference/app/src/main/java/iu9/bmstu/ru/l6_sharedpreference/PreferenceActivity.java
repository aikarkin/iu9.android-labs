package iu9.bmstu.ru.l6_sharedpreference;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class PreferenceActivity extends AppCompatActivity implements SettingsFragment.OnFragmentInteractionListener {
    private static final String TAG = "PreferenceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: init btn action");
        setContentView(R.layout.activity_preference);
        Log.i(TAG, "onCreate: content view set");

        Button saveBtn = findViewById(R.id.settings_btn_save);
        saveBtn.setOnClickListener(view -> this.finish());
        Log.i(TAG, "onCreate: save btn listener set");
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.i(TAG, "onFragmentInteraction: fragment interaction");
    }

}
