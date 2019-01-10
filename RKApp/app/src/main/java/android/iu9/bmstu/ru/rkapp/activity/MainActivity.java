package android.iu9.bmstu.ru.rkapp.activity;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.iu9.bmstu.ru.rkapp.Const;
import android.iu9.bmstu.ru.rkapp.adapter.CurrencyListAdapter;
import android.iu9.bmstu.ru.rkapp.loader.CurrencyLoader;
import android.iu9.bmstu.ru.rkapp.R;
import android.iu9.bmstu.ru.rkapp.entity.CurrencyEntity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<CurrencyEntity>>, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";

    private List<CurrencyEntity> currencyData;
    private CurrencyListAdapter rvAdapter;

    private static String selectedCurrency;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        Button btnDisplay = findViewById(R.id.displayCurrency);
        btnDisplay.setOnClickListener(btn -> loadCurrencyList());
        loadCurrencyList();

        Button btnOpenSite = findViewById(R.id.openWebSite);
        btnOpenSite.setOnClickListener(btn -> {
            Intent openSiteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.apiSite)));
            if(openSiteIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(openSiteIntent);
            } else {
                Log.e(TAG, "Failed to open site. No app provided");
                Toast.makeText(this, "Failed to open site. No app provided", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_item_update) {
            Log.i(TAG, "Menu - Update item selected");
            loadCurrencyList();
        } else if (itemId == R.id.menu_item_settings) {
            Log.i(TAG, "Menu - Settings item selected");
            Intent settingsIntent = new Intent(MainActivity.this, PreferenceActivity.class);
            startActivityForResult(settingsIntent, Const.ActivityReq.settingsChangeReq);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<CurrencyEntity>> onCreateLoader(int id, Bundle args) {
        String apiKey, fsym, tsym;
        int noOfDays;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        fsym = selectedCurrency;
        tsym = preferences.getString(Const.Pref.currency, getResources().getString(R.string.defaultTSym));
        noOfDays = Integer.valueOf(
                preferences.getString(
                        Const.Pref.noOfDays,
                        String.valueOf(getResources().getInteger(R.integer.default_no_of_days))
                )
        );
        apiKey = getResources().getString(R.string.apiKey);

        return new CurrencyLoader(this, generateCurrencyUri(apiKey, fsym, tsym, noOfDays));
    }

    @Override
    public void onLoadFinished(Loader<List<CurrencyEntity>> loader, List<CurrencyEntity> data) {
        CurrencyLoader currencyLoader = (CurrencyLoader) loader;
        if(currencyLoader.hasErrors()) {
            Log.e(TAG, "onLoadFinished: failed to load currency list: " + currencyLoader.getErrorMessage());
            Toast.makeText(this, currencyLoader.getErrorMessage(), Toast.LENGTH_LONG).show();
        } else {
            this.currencyData = data;
            Log.i(TAG, "onLoadFinished: load finished, currency data: " + currencyData);
            initRecyclerView();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<CurrencyEntity>> loader) {
        if(loader.isStarted())
            loader.stopLoading();
        this.currencyData = null;
        if(rvAdapter != null) {
            rvAdapter.setData(null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(currencyData instanceof ArrayList)
            outState.putSerializable(Const.Key.currencyData, (ArrayList<CurrencyEntity>)currencyData);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        startLoader();
    }

    private void startLoader() {
        LoaderManager loaderManager = getLoaderManager();
        Loader<List> newsLoader = loaderManager.getLoader(Const.Loader.currencyLoaderId);

        if(newsLoader == null) {
            Log.i(TAG, "init NewsLoader");
            loaderManager.initLoader(Const.Loader.currencyLoaderId, null, this);
        } else {
            Log.i(TAG, "restart NewsLoader");
            loaderManager.restartLoader(Const.Loader.currencyLoaderId, null, this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadCurrencyList() {
        EditText etCurrency = findViewById(R.id.editTextCurrency);
        selectedCurrency = etCurrency.getText().toString().toUpperCase();
        Set<String> currencySet = new HashSet<>(Arrays.asList(getResources().getStringArray(R.array.currency_names)));
        Log.i(TAG, "onCreate: available currencies: " + String.join(", ", currencySet));
        Log.i(TAG, "onCreate: selected currency: " + selectedCurrency);

        if(currencySet.contains(selectedCurrency)) {
            startLoader();
        } else {
            Toast.makeText(this, "Unknown currency: " + selectedCurrency, Toast.LENGTH_SHORT).show();
        }
    }

    private void initRecyclerView() {
        Log.i(TAG, "initRecyclerView: ");
        TextView loader = findViewById(R.id.loader);
        loader.setVisibility(View.INVISIBLE);
        loader.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

        RecyclerView currencyView = findViewById(R.id.currencyRecyclerView);
        rvAdapter = new CurrencyListAdapter(this, selectedCurrency);
        rvAdapter.setData(currencyData);
        rvAdapter.notifyDataSetChanged();
        currencyView.setAdapter(rvAdapter);
        currencyView.setLayoutManager(new LinearLayoutManager(this));
    }

    private String generateCurrencyUri(String apiKey, String fsym, String tsym, int limit) {
        if (fsym == null || tsym == null || limit == 0)
            return null;

        Uri currencyHistoryUri = Uri.parse(Const.Svc.history)
                .buildUpon()
                .appendQueryParameter(Const.Key.apiKey, apiKey)
                .appendQueryParameter(Const.Key.fsym, fsym)
                .appendQueryParameter(Const.Key.tsym, tsym)
                .appendQueryParameter(Const.Key.limit, String.valueOf(limit))
                .build();

        return currencyHistoryUri.toString();
    }

}