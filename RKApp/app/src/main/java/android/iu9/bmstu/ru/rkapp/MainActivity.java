package android.iu9.bmstu.ru.rkapp;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.iu9.bmstu.ru.rkapp.entity.CurrencyEntity;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<CurrencyEntity>> {
    private static final String TAG = "MainActivity";
    private static final String CURRENCY_KEY = "currencyData";
    private static final String CURRENCY_HISTORY_SVC_URL = "https://min-api.cryptocompare.com/data/histoday";
    private static final int SETTINGS_CHANGE_REQUEST = 1;
    private static final int CURRENCY_LOADER_ADAPTER = 1;

    private List<CurrencyEntity> currencyData;
    private CurrencyListAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENCY_KEY)) {
            currencyData =
                    (ArrayList<CurrencyEntity>) savedInstanceState.getSerializable(CURRENCY_KEY);
//            initRecyclerView();
        }

        LoaderManager loaderManager = getLoaderManager();
        Loader<List> newsLoader = loaderManager.getLoader(CURRENCY_LOADER_ADAPTER);
        if(newsLoader == null) {
            Log.i(TAG, "init NewsLoader");
            loaderManager.initLoader(CURRENCY_LOADER_ADAPTER, null, this);
        } else {
            Log.i(TAG, "restart NewsLoader");
            loaderManager.restartLoader(CURRENCY_LOADER_ADAPTER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_item_update) {
            Log.i(TAG, "Menu - Update item selected");
        } else if (itemId == R.id.menu_item_settings) {
            Log.i(TAG, "Menu - Settings item selected");
            Intent emailIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(emailIntent, SETTINGS_CHANGE_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_CHANGE_REQUEST) {
            Log.i(TAG, "Settings changed");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<List<CurrencyEntity>> onCreateLoader(int id, Bundle args) {
        return new CurrencyLoader(this, generateCurrencyUri("USD", "RUB", 10));
    }

    @Override
    public void onLoadFinished(Loader<List<CurrencyEntity>> loader, List<CurrencyEntity> data) {
        this.currencyData = data;
        Log.i(TAG, "onLoadFinished: load finished, currency data: " + currencyData);
        initRecyclerView();
    }

    @Override
    public void onLoaderReset(Loader<List<CurrencyEntity>> loader) {
        if(loader.isStarted())
            loader.stopLoading();
        this.currencyData = null;
        rvAdapter.setData(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(currencyData instanceof ArrayList)
            outState.putSerializable(CURRENCY_KEY, (ArrayList<CurrencyEntity>)currencyData);

        super.onSaveInstanceState(outState);
    }

    private void initRecyclerView() {
        Log.i(TAG, "initRecyclerView: ");
        TextView loader = findViewById(R.id.loader);
        loader.setVisibility(View.INVISIBLE);
        loader.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

        RecyclerView currencyView = findViewById(R.id.currencyRecyclerView);
        rvAdapter = new CurrencyListAdapter();
        rvAdapter.setData(currencyData);
        currencyView.setAdapter(rvAdapter);
        currencyView.setLayoutManager(new LinearLayoutManager(this));
    }

    private String generateCurrencyUri(String fsym, String tsym, int limit) {
        if (fsym == null || tsym == null || limit == 0)
            return null;

        Uri currencyHistoryUri = Uri.parse(CURRENCY_HISTORY_SVC_URL)
                .buildUpon()
//                .appendQueryParameter("aggregate", "daily")
                .appendQueryParameter("fsym", fsym)
                .appendQueryParameter("tsym", tsym)
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();

        return currencyHistoryUri.toString();
    }
}
