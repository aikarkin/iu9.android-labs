package labs.android.iu9.bmstu.ru.lab7;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class TextLoader extends AsyncTaskLoader<StringBuilder> {
    private static final String TAG = "TextLoader";

    private HttpURLConnection connection;
    private InputStream dataStream;
    private StringBuilder response;
    private String url;

    public TextLoader(Context ctx, String url) {
        super(ctx);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        // If we already has articles, deliver it immediately
        if(response != null && response.length() > 0) {
            deliverResult(response);
        }

        if(takeContentChanged() || response == null) {
            forceLoad();
        }
    }

    @Override
    public StringBuilder loadInBackground() {
        if(response == null) {
            try {
                response = getResponseFromUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return response;
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected boolean onCancelLoad() {
        boolean res = super.onCancelLoad();
        onReleaseResources();

        return res;
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if(response != null) {
            onReleaseResources();
            response = null;
        }
    }

    @Override
    public void deliverResult(StringBuilder response) {
        if(isReset() && response != null) {
            onReleaseResources();
        }

        if(isStarted()) {
            super.deliverResult(response);
        }

        if(response != null)
            onReleaseResources();
    }

    private void onReleaseResources() {
        if(dataStream != null) {
            try {
                dataStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(connection != null) {
            connection.disconnect();
        }
    }
    private StringBuilder getResponseFromUrl(String urlStr) throws IOException {
        Log.i(TAG, "getResponseFromUrl: fetch from: " + urlStr);

        URL url = new URL(urlStr);
        connection = (HttpURLConnection) url.openConnection();
        StringBuilder res = new StringBuilder();


        dataStream = connection.getInputStream();
        Scanner scanner = new Scanner(dataStream).useDelimiter("\\A");

        while (scanner.hasNext()) {
            res.append(scanner.next());
        }

        Log.i(TAG, "getResponseFromUrl: response: " + res.toString());


        return res;
    }
}
