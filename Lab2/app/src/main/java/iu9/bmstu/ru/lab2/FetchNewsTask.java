package iu9.bmstu.ru.lab2;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FetchNewsTask extends AsyncTask<URL, Void, List<ArticleEntity>> {
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

    protected List<ArticleEntity> doInBackground(URL... urls) {
        List<ArticleEntity> entities = null;

        try {
            StringBuilder txtOutBuilder = new StringBuilder();
            URL newsUrl = Arrays.asList(urls).get(0);
            String content = getResponseFromUrl(newsUrl);
            entities = ArticleEntity.parseEntitiesFromJson(content);
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }

        return entities;
    }
}
