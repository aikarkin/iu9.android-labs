package android.iu9.bmstu.ru.rkapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.iu9.bmstu.ru.rkapp.entity.CurrencyEntity;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class CurrencyLoader extends AsyncTaskLoader<List<CurrencyEntity>> {
    private static final String TAG = "CurrencyLoader";
    List<CurrencyEntity> entities;
    private String url;
    private HttpURLConnection connection;
    private InputStream dataStream;

    CurrencyLoader(Context ctx, String url) {
        super(ctx);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        Log.i(TAG, "Start fetch from: " + this.url);

        // If we already has articles, deliver it immediately
        if(entities != null) {
            deliverResult(entities);
        }

        if(takeContentChanged() || entities == null) {
            forceLoad();
        }
    }

    @Override
    public List<CurrencyEntity> loadInBackground() {
        Log.i(TAG, "loadInBackground");

        if(entities == null) {
            try {
                String content = getResponseFromUrl(url);
                entities = CurrencyEntity.parse(content);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.i(TAG, "Exception: " + e.getMessage());
                entities = null;
            }
        }

        return entities;
    }
    @Override
    protected void onStopLoading() {
        Log.i(TAG, "onCancelLoad");
        cancelLoad();
    }

    @Override
    protected boolean onCancelLoad() {
        Log.i(TAG, "onCancelLoad");
        boolean res = super.onCancelLoad();
        onReleaseResources();

        return res;
    }

    @Override
    protected void onReset() {
        Log.i(TAG, "onReset");
        super.onReset();

        onStopLoading();

        if(entities != null) {
            onReleaseResources();
            entities = null;
        }
    }

    @Override
    public void deliverResult(List<CurrencyEntity> entities) {
        Log.i(TAG, "deliverResult");
        if(isReset() && entities != null) {
            // Data fetch was interrupted, stream and connection should be closed and etc.
            onReleaseResources();
        }

        if(isStarted()) {
            super.deliverResult(entities);
        }

        if(entities != null)
            onReleaseResources();
    }

    private void onReleaseResources() {
        Log.i(TAG, "onReleaseResources");
        if(dataStream != null) {
            try {
                dataStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Unable to close InputStream");
            }
        }
        if(connection != null) {
            connection.disconnect();
        }
    }

    private String getResponseFromUrl(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        connection = (HttpURLConnection) url.openConnection();
        StringBuilder res = new StringBuilder();


        InputStream in = connection.getInputStream();
        Scanner scanner = new Scanner(in).useDelimiter("\\A");

        while (scanner.hasNext()) {
            res.append(scanner.next());
        }

        return res.toString();
    }

}
