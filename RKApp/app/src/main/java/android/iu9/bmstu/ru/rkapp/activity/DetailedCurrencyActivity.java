package android.iu9.bmstu.ru.rkapp.activity;

import android.content.Intent;
import android.iu9.bmstu.ru.rkapp.R;
import android.iu9.bmstu.ru.rkapp.task.FetchDetailedCurrencyTask;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DetailedCurrencyActivity extends AppCompatActivity {
    private static final String TAG = "DetailedCurrencyActivity";
    public static final String DETAILED_CURRENCY_URL = "https://min-api.cryptocompare.com/data/pricehistorical";
    private static final Map<String, Integer> CUR_TO_RES_ID = new HashMap<>();
    static {
        CUR_TO_RES_ID.put("USD", R.id.usdVal);
        CUR_TO_RES_ID.put("RUB", R.id.rubVal);
        CUR_TO_RES_ID.put("EUR", R.id.eurVal);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_currency);

        Intent data = getIntent();
        long ts = data.getLongExtra("ts", 0L);
        String fsym = data.getStringExtra("fsym");

        if(ts > 0 || fsym == null) {
            Log.e(TAG, "onCreate: invalid request data");
            FetchDetailedCurrencyTask asyncTask = new FetchDetailedCurrencyTask();
            String[] availableCurrencies = getResources().getStringArray(R.array.currency_names);
            String apiKey = getResources().getString(R.string.apiKey);
            Uri currencyUri = Uri.parse(DETAILED_CURRENCY_URL)
                    .buildUpon()
                    .appendQueryParameter("apiKey", apiKey)
                    .appendQueryParameter("fsym", fsym)
                    .appendQueryParameter("tsyms", String.join(",", availableCurrencies))
                    .appendQueryParameter("ts", String.valueOf(ts))
                    .build();

            asyncTask.execute(currencyUri);

            try {
                Double[] values = asyncTask.get();

                for (int i = 0; i < availableCurrencies.length; i++) {
                    String currency = availableCurrencies[i];
                    if(CUR_TO_RES_ID.containsKey(currency)) {
                        TextView tvCurVal = findViewById(CUR_TO_RES_ID.get(currency));
                        tvCurVal.setText(String.format(Locale.ENGLISH, "%.3f", values[i]));
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }


}
