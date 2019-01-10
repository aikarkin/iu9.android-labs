package android.iu9.bmstu.ru.rkapp.activity;

import android.iu9.bmstu.ru.rkapp.R;
import android.iu9.bmstu.ru.rkapp.frag.SettingsFragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class PreferenceActivity extends AppCompatActivity implements SettingsFragment.OnFragmentInteractionListener {
    private static final String TAG = "PreferenceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.i(TAG, "onFragmentInteraction: fragment interaction");
    }

}
