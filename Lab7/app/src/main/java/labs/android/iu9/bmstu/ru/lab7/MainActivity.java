package labs.android.iu9.bmstu.ru.lab7;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String BUNDLE_KEY_CURRENCY_SYMBOLS = "CURRENCY_SYMBOLS";
    private static final String BUNDLE_KEY_CURRENCY_EXCHANGES = "CURRENCY_EXCHANGES";
    private static final int CURRENCY_SYMS_LOADER_ID = 1;
    private static final int CURRENCY_EXCHANGES_LIST_LOADER_ID = 2;

    private static SQLiteDatabase currencyDb;

    private HashSet<String> currencySymbols;
    private ArrayList<CurrencyExchange> currencyExchanges;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null)
            loadPreviousState(savedInstanceState);

        DbHelper dbHelper = new DbHelper(this);
        currencyDb = dbHelper.getWritableDatabase();

        if(currencyExchanges != null) {
            Log.i(TAG, "onCreate: exchanges already loaded");
            fillDatabase();
            logSqlData();
        } else if(currencySymbols != null) {
            initExchangesLoader();
        }  else {
            initCurrencySymsLoader();
        }
    }

    private void fillDatabase() {
        Log.i(TAG, "fillDatabase: filling database");
        ContentValues insertedValues = new ContentValues();

        for(CurrencyExchange exchange :currencyExchanges) {
            insertedValues.put(DbContract.dbEntry.CURRENCY, exchange.getFsym());
            insertedValues.put(DbContract.dbEntry.TO_EUR, exchange.getTsyms().get("EUR"));
            insertedValues.put(DbContract.dbEntry.TO_USD, exchange.getTsyms().get("USD"));
            insertedValues.put(DbContract.dbEntry.TO_RUB, exchange.getTsyms().get("RUB"));

            currencyDb.insert(DbContract.dbEntry.TABLE_NAME, null, insertedValues);
        }

        Log.i(TAG, "fillDatabase: database has filled successfully");
    }

    private void logSqlData() {
        Cursor cursor = currencyDb.query(
                DbContract.dbEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DbContract.dbEntry.CURRENCY
        );

        Log.i(TAG, "logSqlData: select from '" + DbContract.dbEntry.TABLE_NAME + "'");

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            Log.i(TAG, "logSqlData: " + String.format(
                    "{currency: %s, eur: %s, usd: %s, rub: %s}",
                    cursor.getDouble(cursor.getColumnIndex(DbContract.dbEntry.CURRENCY)),
                    cursor.getDouble(cursor.getColumnIndex(DbContract.dbEntry.TO_EUR)),
                    cursor.getDouble(cursor.getColumnIndex(DbContract.dbEntry.TO_USD)),
                    cursor.getDouble(cursor.getColumnIndex(DbContract.dbEntry.TO_RUB))
            ));
        }

        cursor.close();
    }

    @SuppressWarnings("unchecked")
    private void loadPreviousState(@NonNull Bundle savedInstanceState) {
        Object exchangesObj = savedInstanceState.get(BUNDLE_KEY_CURRENCY_EXCHANGES);
        Object namesObj = savedInstanceState.get(BUNDLE_KEY_CURRENCY_SYMBOLS);
        if(exchangesObj instanceof ArrayList) {
            currencyExchanges = (ArrayList<CurrencyExchange>) exchangesObj;
        }

        if(namesObj instanceof HashSet) {
            currencySymbols = (HashSet<String>) namesObj;
        }
    }

    private void initCurrencySymsLoader() {
        Log.i(TAG, "initCurrencySymsLoader: ");
        LoaderManager loaderManager = getLoaderManager();
        Loader<StringBuilder> loader = loaderManager.getLoader(CURRENCY_SYMS_LOADER_ID);
        BaseLoaderCallbacks<HashSet<String>> currencySymbolsCb = new BaseLoaderCallbacks<HashSet<String>>() {
            @Override
            HashSet<String> parse(StringBuilder data) {
                Log.i(TAG, "parse: currencySymbolsCb");
                try {
                    return CurrencyUtils.parseCurrencySymbols(data.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "parse: failed to parse currency symbols");
                }
                return null;
            }

            @Override
            void whenParsed(HashSet<String> data) {
                Log.i(TAG, "whenParse: currencySymbolsCb");
                currencySymbols = data;
                if(data != null) {
                    initExchangesLoader();
                }
            }
        };
        currencySymbolsCb.setContext(this);

        if(loader == null) {
            loaderManager.initLoader(CURRENCY_SYMS_LOADER_ID, null, currencySymbolsCb);
        } else {
            loaderManager.restartLoader(CURRENCY_SYMS_LOADER_ID, null, currencySymbolsCb);
        }
    }

    private void initExchangesLoader() {
        Log.i(TAG, "initExchangesLoader: ");
        LoaderManager loaderManager = getLoaderManager();
        Loader<StringBuilder> loader = loaderManager.getLoader(CURRENCY_EXCHANGES_LIST_LOADER_ID);
        BaseLoaderCallbacks<ArrayList<CurrencyExchange>> currencyExchangesCb = new BaseLoaderCallbacks<ArrayList<CurrencyExchange>>() {

            @Override
            ArrayList<CurrencyExchange> parse(StringBuilder data) {
                Log.i(TAG, "parse: currency exchange cb");
                try {
                    return CurrencyUtils.parseCurrencyExchange(data.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG, "parse: failed to parse currency exchanges");
                }

                return null;
            }

            @Override
            void whenParsed(ArrayList<CurrencyExchange> data) {
                Log.i(TAG, "whenParsed: currency exchange cb");
                currencyExchanges = data;
                fillDatabase();
                logSqlData();
            }
        };

        currencyExchangesCb.setContext(this);

        if(loader == null) {
            Log.i(TAG, "initExchangesLoader: init loader");
            loaderManager.initLoader(CURRENCY_EXCHANGES_LIST_LOADER_ID, null, currencyExchangesCb);
        } else {
            Log.i(TAG, "initExchangesLoader: restart loader");
            loaderManager.restartLoader(CURRENCY_EXCHANGES_LIST_LOADER_ID, null, currencyExchangesCb);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState: save currency symbols and currency exchanges");

        outState.putSerializable(BUNDLE_KEY_CURRENCY_SYMBOLS, currencySymbols);
        outState.putSerializable(BUNDLE_KEY_CURRENCY_EXCHANGES, currencyExchanges);

        super.onSaveInstanceState(outState);
    }
}
