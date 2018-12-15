package iu9.bmstu.ru.lab3;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String NEWS_URI_STR = "https://newsapi.org/v2/top-headlines";
    private static final String API_KEY = "95718e78b75c4f17bd4f253feb2503a7";
    private static final String COUNTRY_CODE = "ru";
    private static final String CATEGORY = "business";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // build news uri
        Uri newsUri = Uri.parse(NEWS_URI_STR)
                .buildUpon()
                .appendQueryParameter("apiKey", API_KEY)
                .appendQueryParameter("country", COUNTRY_CODE)
                .appendQueryParameter("category", CATEGORY)
                .build();

        try {
            RecyclerView rView = findViewById(R.id.recycler_view);
            NewsListAdapter adapter = new NewsListAdapter(this, newsUri.toString());
            rView.setAdapter(adapter);
            rView.setLayoutManager(new LinearLayoutManager(this));
        } catch (MalformedURLException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
