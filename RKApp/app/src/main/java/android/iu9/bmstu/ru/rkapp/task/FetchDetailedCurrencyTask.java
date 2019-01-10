package android.iu9.bmstu.ru.rkapp.task;

import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FetchDetailedCurrencyTask extends AsyncTask<Uri, Void, Double[]> {
    @Override
    protected Double[] doInBackground(Uri... uris) {
        String tsymsParam = uris[0].getQueryParameter("tsyms");
        String fsym = uris[0].getQueryParameter("fsym");

        if(tsymsParam != null) {
            String[] tsyms = tsymsParam.split(",");
            try {
                String content = fetchForce(uris[0]);
                return parseCurrencies(fsym, tsyms, content);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        return new Double[0];
    }

    private Double[] parseCurrencies(String fsym, String[] tsyms, String content) throws JSONException {
        Double[] values = new Double[tsyms.length];

        JSONObject jsonObj = (new JSONObject(content)).getJSONObject(fsym);
        for (int i = 0; i < values.length; i++) {
            values[i] = jsonObj.getDouble(tsyms[i]);
        }

        return values;
    }

    private static String fetchForce(Uri uri) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
        StringBuilder res = new StringBuilder();

        try {
            InputStream in = connection.getInputStream();
            Scanner scanner = new Scanner(in).useDelimiter("\\A");

            while (scanner.hasNext()) {
                res.append(scanner.next());
            }
        } finally {
            connection.disconnect();
        }

        return res.toString();
    }



}
