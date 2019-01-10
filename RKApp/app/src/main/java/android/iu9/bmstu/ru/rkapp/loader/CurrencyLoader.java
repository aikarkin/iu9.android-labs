package android.iu9.bmstu.ru.rkapp.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.iu9.bmstu.ru.rkapp.entity.CurrencyEntity;
import android.iu9.bmstu.ru.rkapp.exception.BadApiRequestException;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class CurrencyLoader extends AsyncTaskLoader<List<CurrencyEntity>> {
    private static final String TAG = "CurrencyLoader";
    private List<CurrencyEntity> entities;
    private String url;
    private HttpURLConnection connection;
    private InputStream dataStream;
    private boolean hasErrors;
    private String errorMsg;

    public CurrencyLoader(Context ctx, String url) {
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
                entities = CurrencyEntity.parse(getContext(), content);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "loadInBackground: ", e);
                entities = null;
            } catch (BadApiRequestException e) {
                Log.e(TAG, "loadInBackground: ", e);
                hasErrors = true;
                errorMsg = e.getMessage();
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


        dataStream = connection.getInputStream();
        Scanner scanner = new Scanner(dataStream).useDelimiter("\\A");

        while (scanner.hasNext()) {
            res.append(scanner.next());
        }

        return res.toString();
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public String getErrorMessage() {
        return errorMsg;
    }
}
