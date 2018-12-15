package iu9.bmstu.ru.l6_sharedpreference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";

    ArticleListAdapter recycleViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // load shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean mustPicturesShown = preferences.getBoolean("must_pictures_shown", true);
        preferences.registerOnSharedPreferenceChangeListener(this);

        // set locale
        Resources res = getResources();
        String curLocale = preferences.getString("lang_gui", res.getString(R.string.pref_default_gui_lang));
        Log.i(TAG, "changeLocale: cur locale: " + curLocale);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(Locale.forLanguageTag(curLocale));
        resources.updateConfiguration(configuration, this.getResources().getDisplayMetrics());

        // build news uri
        Uri newsUri = buildApiUriFromPreference(preferences);

        try {
            RecyclerView rView = findViewById(R.id.recycler_view);
            recycleViewAdapter = new ArticleListAdapter(this, newsUri.toString(), mustPicturesShown);
            rView.setAdapter(recycleViewAdapter);
            rView.setLayoutManager(new LinearLayoutManager(this));
        } catch (MalformedURLException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.settings_item) {
            Intent preferenceIntent = new Intent(MainActivity.this, PreferenceActivity.class);
            Log.i(TAG, "onOptionsItemSelected: starting pref activity");
            startActivity(preferenceIntent);
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "onSharedPreferenceChanged: change preference key: " + key);

        switch (key) {
            case "lang_gui": {
                changeLocale(sharedPreferences);
                break;
            }
            case "news_api_key":
            case "news_sources":
            case "must_pictures_shown": {
                try {
                    refreshRecyclerView(sharedPreferences);
                } catch (InterruptedException | ExecutionException | MalformedURLException e) {
                    Log.i(TAG, "onSharedPreferenceChanged: unable to refresh recycler view: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            }
            default: {
                Log.i(TAG, "onSharedPreferenceChanged: unknown preference key: " + key);
                break;
            }
        }
    }

    private void refreshRecyclerView(SharedPreferences preferences) throws InterruptedException, ExecutionException, MalformedURLException {
        boolean mustPicturesShown = preferences.getBoolean("must_pictures_shown", true);
        Uri newsUri = buildApiUriFromPreference(preferences);
        Log.i(TAG, "refreshRecyclerView: refresh recycle view - fetch news from: " + newsUri);
        recycleViewAdapter.setPicturesShown(mustPicturesShown);
        recycleViewAdapter.setNewsUri(newsUri.toString());
        recycleViewAdapter.refresh();
    }


    private void changeLocale(SharedPreferences preferences) {
        Resources res = getResources();
        String curLocale = preferences.getString("lang_gui", res.getString(R.string.pref_default_gui_lang));
        Log.i(TAG, "changeLocale: cur locale: " + curLocale);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(Locale.forLanguageTag(curLocale));
        resources.updateConfiguration(configuration, this.getResources().getDisplayMetrics());
        finish();
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
    }

    private Uri buildApiUriFromPreference(SharedPreferences preferences) {
        Resources res = this.getResources();
        String[] defaultSources = res.getStringArray(R.array.sources_default_values);
        String defaultApiKey = res.getString(R.string.pref_default_api_key);
        String newsUri = res.getString(R.string.news_uri);

        String apiKey = preferences.getString("news_api_key", defaultApiKey);
        Set<String> sources = preferences.getStringSet("news_sources", new HashSet<>(Arrays.asList(defaultSources)));

        return Uri.parse(newsUri)
                .buildUpon()
                .appendQueryParameter("apiKey", apiKey)
                .appendQueryParameter("sources", String.join(",", sources))
                .build();
    }
}
