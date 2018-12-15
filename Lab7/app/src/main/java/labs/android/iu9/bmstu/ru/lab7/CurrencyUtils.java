package labs.android.iu9.bmstu.ru.lab7;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public final class CurrencyUtils {
    private static final String TAG = "CurrencyUtils";

    private enum JsonKeys {
        Data,
        Symbol
    }

    private enum QueryParams {
        api_key,
        fsyms,
        tsyms
    }

    private static final String COMMA = ",";
    private static final String SCHEME = "https";

    private CurrencyUtils() {}

    public static HashSet<String> parseCurrencySymbols(String content) throws JSONException {
        HashSet<String> names = new HashSet<>();
        JSONArray jsonArray = new JSONObject(content).getJSONArray(JsonKeys.Data.name());

        for (int i = 0; i < jsonArray.length(); i++) {
            names.add(jsonArray.getJSONObject(i).getString(JsonKeys.Symbol.name()));
        }

        Log.i(TAG, "parseCurrencySymbols: parsed symbols: " + names);

        return names;
    }

    public static ArrayList<CurrencyExchange> parseCurrencyExchange(String content) throws JSONException {
        JSONObject json = new JSONObject(content);
        ArrayList<CurrencyExchange> exchanges = new ArrayList<>();

        for (Iterator<String> fsymIt = json.keys(); fsymIt.hasNext(); ) {
            String fsymKey = fsymIt.next();
            CurrencyExchange exchange = new CurrencyExchange(fsymKey);
            JSONObject tsymsJson = json.getJSONObject(fsymKey);

            for (Iterator<String> tsymIt = tsymsJson.getJSONObject(fsymKey).keys(); tsymIt.hasNext(); ) {
                exchange.addTsym(fsymKey, tsymsJson.getDouble(tsymIt.next()));
            }

            exchanges.add(exchange);
        }

        Log.i(TAG, "parseCurrencyExchange: parsed exchanges: " + exchanges);

        return exchanges;
    }

    public static String buildCoinListUrl(Context ctx) {
        String coinListPath = ctx.getString(R.string.allCoinsPath);

        return baseBuilder(ctx)
                .appendPath(coinListPath)
                .appendQueryParameter(QueryParams.api_key.name(), apiKey(ctx))
                .build()
                .toString();
    }

    public static String buildPriceMultiUri(Context ctx, Iterable<String> fsymsIt, Iterable<String> tsymsIt) {
        String priceMultiPath = ctx.getString(R.string.mulSymbolsPath);
        String fsymsVal = String.join(COMMA, fsymsIt);
        String tsymsVal = String.join(COMMA, tsymsIt);

        return baseBuilder(ctx)
                .appendPath(priceMultiPath)
                .appendQueryParameter(QueryParams.fsyms.name(), fsymsVal)
                .appendQueryParameter(QueryParams.tsyms.name(), tsymsVal)
                .appendQueryParameter(QueryParams.api_key.name(), apiKey(ctx))
                .build()
                .toString();
    }

    private static String apiKey(Context ctx) {
        return ctx.getString(R.string.apiKey);
    }

    private static Uri.Builder baseBuilder(Context ctx) {
        String authority = ctx.getString(R.string.apiHost);

        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(authority);
    }
}
