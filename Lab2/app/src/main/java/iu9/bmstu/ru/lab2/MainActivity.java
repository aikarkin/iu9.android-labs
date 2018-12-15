package iu9.bmstu.ru.lab2;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TableLayout tableLayout;

    private List<ArticleEntity> newsList;
    private int articleCounter = 0;

    private static final float NEWS_IMAGE_WIDTH = 150;
    private static final float NEWS_IMAGE_HEIGHT = 70;
    private static final String[] SUPPORTED_LANGS = {"en", "ru"};


    private String getCurrentLocaleCode() {
        return this.getResources().getConfiguration().locale.getLanguage();
    }

    private void changeLocale() {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        int curLocaleIdx = Arrays.binarySearch(SUPPORTED_LANGS, getCurrentLocaleCode());
        configuration.setLocale(Locale.forLanguageTag(SUPPORTED_LANGS[(curLocaleIdx + 1) % SUPPORTED_LANGS.length]));
        resources.updateConfiguration(configuration, this.getResources().getDisplayMetrics());
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        finish();
        updateNews();
    }

    private URL buildUrl() throws MalformedURLException {
        String apiKey = getString(R.string.api_key),
                category = getString(R.string.default_category),
                country = getString(R.string.defualt_country),
                newsUriStr = getString(R.string.api_news_uri);

        Uri newsUri = Uri.parse(newsUriStr).buildUpon()
                .appendQueryParameter("country", getCurrentLocaleCode().equals("ru") ? "ru" : "us")
                .appendQueryParameter("category", category)
                .appendQueryParameter("apiKey", apiKey)
                .build();

        Log.i(TAG, "News API url: "  + newsUri.toString());

        return new URL(newsUri.toString());
    }

    private int getOrientation() {
        return this.getResources().getConfiguration().orientation;
    }

    private List<ArticleEntity> fetchNews() throws ExecutionException, InterruptedException, MalformedURLException {
        URL newsUrl = buildUrl();
        FetchNewsTask fetchTask = new FetchNewsTask();
        fetchTask.execute(newsUrl);
        return fetchTask.get();
    }

    private void renderNewsLandscapeLayout() {
        if(newsList.size() > 0) {
            Button prevButton = findViewById(R.id.btn_prev),
                    nextButton = findViewById(R.id.btn_next);

            TextView title = findViewById(R.id.article_title),
                    content = findViewById(R.id.article_content);

            if (articleCounter < 0)
                articleCounter = newsList.size() - 1;
            else if (articleCounter > newsList.size() - 1)
                articleCounter = 0;

            ArticleEntity entity = newsList.get(articleCounter);

            String titleVal = entity.getTitle() == null ? "" : entity.getTitle();
            String contentVal = entity.getContent() == null ? "" : entity.getContent();

            title.setText(titleVal);
            content.setText(contentVal);

            ImageView articleImage = findViewById(R.id.article_image);

            try {
                articleImage.setImageBitmap(entity.getBitmap(this));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to set image bitmap - " + e.getMessage());
            }


            prevButton.setOnClickListener((btn) -> {
                articleCounter--;
                renderNewsLandscapeLayout();
            });

            nextButton.setOnClickListener((btn) -> {
                articleCounter++;
                renderNewsLandscapeLayout();
            });
        } else {
            Log.w(TAG, "Failed to render news. Articles list is empty.");


        }
    }

    private void renderNews() {
        if(getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            Log.i(TAG, "Portrait orientation");
            if(tableLayout != null) {
                Log.i(TAG, "removing all views");
                tableLayout.removeAllViews();
            }
            renderNewsPortraitLayout();
        } else {
            articleCounter = 0;
            renderNewsLandscapeLayout();
        }
    }

    private void updateNews() {
        try {
            newsList = fetchNews();
            Log.i(TAG, "Got news list: " + newsList.toString());

            renderNews();
        } catch (ExecutionException | InterruptedException | MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to fetch news.");
        }
    }

    private void renderNewsPortraitLayout() {
        Log.i(TAG, "renderNewsPortraitLayout()");
        tableLayout = new TableLayout(this);
        tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        tableLayout.setStretchAllColumns(true);

        LinearLayout mainLayout = findViewById(R.id.news_container);
        mainLayout.addView(tableLayout);

        newsList.forEach(this::appendNewsPortrait);
    }

    private void appendNewsPortrait(ArticleEntity news) {
        Log.i(TAG, "append article: " + news.toString());
        TableRow tRow = new TableRow(this);
        tRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        tRow.setPadding(0, 5, 0, 5);

        ImageView newsImg = new ImageView(this);

        newsImg.setLayoutParams(new TableRow.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, NEWS_IMAGE_WIDTH, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, NEWS_IMAGE_HEIGHT, getResources().getDisplayMetrics()),
                1
        ));
        try {
            newsImg.setImageBitmap(news.getBitmap(this));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to set image bitmap - " + e.getMessage());
        }

        TextView newsContent = new TextView(this);
        newsContent.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1));
        newsContent.setPadding(15, 5 ,5 ,5);
        newsContent.setTextSize(10);

        String dateStr = "";

        if(news.getPublished() != null) {
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            dateStr = simpleDate.format(news.getPublished());
        }

        String titleVal = news.getTitle() == null ? "" : news.getTitle();

        String newsHtml = "<i>" + titleVal + "</i><br/><br/>" +
                (news.getSource() != null ? news.getSource() + ", " : "") + dateStr;

        newsContent.setText(Html.fromHtml(newsHtml));

        tRow.addView(newsImg);
        tRow.addView(newsContent);

        tableLayout.addView(tRow);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateNews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_upd) {
            updateNews();
            return true;
        } else if (itemId == R.id.action_change_lang) {
            changeLocale();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        renderNews();
    }
    
}
