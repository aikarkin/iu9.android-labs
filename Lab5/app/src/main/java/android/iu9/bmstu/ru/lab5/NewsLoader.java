package android.iu9.bmstu.ru.lab5;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class NewsLoader extends AsyncTaskLoader<ArrayList<ArticleEntity>> {
    private static final String TAG = "NewsLoader";
    private HttpURLConnection connection;
    private InputStream dataStream;
    private String newsUrl;
    private ArrayList<ArticleEntity> articles;

    NewsLoader(Context ctx, String url) {
        super(ctx);
        Log.i(TAG, "CONSTRUCT 'NewsLoader'");
        this.newsUrl = url;
    }

    @Override
    protected void onStartLoading() {
        Log.i(TAG, "onStartLoading");

        // If we already has articles, deliver it immediately
        if(articles != null) {
            deliverResult(articles);
        }

        if(takeContentChanged() || articles == null) {
            forceLoad();
        }
    }

    @Override
    public ArrayList<ArticleEntity> loadInBackground() {
        Log.i(TAG, "loadInBackground");

        if(articles == null) {
            try {
                String content = getResponseFromUrl(newsUrl);
                articles = ArticleEntity.parseEntitiesFromJson(content);

                Log.i(TAG, "got entities: " + articles.toString());

                for (ArticleEntity entity : articles) {
                    Bitmap bmp = getBitmap(entity);
                    if (bmp != null) {
                        Log.i(TAG, "Setting bitmap for '" + entity.toString() + "'");
                        entity.setBitmap(bmp);
                    } else {
                        Log.i(TAG, "Unable to fetch image for '" + entity.toString() + "', skipping it.");
                    }
                }

            } catch (IOException | JSONException | ParseException e) {
                e.printStackTrace();
                Log.i(TAG, "Exception: " + e.getMessage());
            }
        }

        return articles;
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

        if(articles != null) {
            onReleaseResources();
            articles = null;
        }
    }

    @Override
    public void deliverResult(ArrayList<ArticleEntity> entities) {
        Log.i(TAG, "deliverResult");
        if(isReset() && articles != null) {
            // Data fetch was interrupted, stream and connection should be closed and etc.
            onReleaseResources();
        }

        if(isStarted()) {
            super.deliverResult(entities);
        }

        if(articles != null)
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

    private Bitmap getBitmap(ArticleEntity entity) throws IOException {
        Uri uri = Uri.parse(entity.getImageUrl()).buildUpon().build();
        String uriStr = uri.toString();
        Bitmap bmp = null;

        if(uriStr != null) {
            dataStream = new java.net.URL(uriStr).openStream();
            bmp = BitmapFactory.decodeStream(dataStream);
        }

        return bmp;
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
