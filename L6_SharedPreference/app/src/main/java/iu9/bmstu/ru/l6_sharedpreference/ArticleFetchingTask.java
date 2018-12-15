package iu9.bmstu.ru.l6_sharedpreference;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class ArticleFetchingTask extends AsyncTask<URL, Void, List<Article>> {
    private static final String TAG = "FetchNewsTask";

    private static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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

    protected List<Article> doInBackground(URL... urls) {
        Log.i(TAG, "doInBackground");
        List<Article> entities = null;

        try {
            URL newsUrl = Arrays.asList(urls).get(0);
            String content = getResponseFromUrl(newsUrl);
            entities = Article.parseEntitiesFromJson(content);
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }

        return entities;
    }
}
