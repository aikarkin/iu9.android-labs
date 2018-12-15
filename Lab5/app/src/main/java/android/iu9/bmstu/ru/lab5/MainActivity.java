package android.iu9.bmstu.ru.lab5;

import android.app.LoaderManager;
import android.content.Loader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<ArticleEntity>> {
    private static final String TAG = "MainActivity";

    private static final int NEWS_LOADER_ID = 1;

    public static final String NEWS_URI = "https://newsapi.org/v2/top-headlines?country=us&category=business&apiKey=95718e78b75c4f17bd4f253feb2503a7";
    public static final String ARTICLE_KEY = "article";
    public static final String COUNTER_KEY = "article_counter";
    private static final String TEXT_VIEW_KEY = "textview_content";

    private ArrayList<ArticleEntity> articles;
    private ArticleEntity curArticle;
    private int articleCounter = 0;
    private boolean isArticleRendered = false;
    // * нестатическая переменная (onSaveInstanceState) - данные слетают при повороте, в остальных случаях остаются
    // * статическая - данные никогда не слетают
    // ---
    // * при повороте: ... -> onDestroy() -> onCreate() -> ...
    // * при потрере фокуса: onPause() -> onStop() -> onRestart() -> onStart() -> ...
//    private String tvContent = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isArticleRendered = false;

        if(savedInstanceState != null) {
            Log.i(TAG, "There is saved data");
            if(savedInstanceState.containsKey(ARTICLE_KEY)) {
                Object obj = savedInstanceState.get(ARTICLE_KEY);
                if(obj instanceof ArticleEntity) {
                    Log.i(TAG, "Restore article from saved instance state");
                    curArticle = (ArticleEntity) obj;
                    renderArticle(curArticle);
                    isArticleRendered = true;
                } else {
                    Log.w(TAG, "In '" + ARTICLE_KEY + "' bundle stored is not ArticleEntity. Value will be ignored.");
                }
            } else {
                Log.i(TAG, "No article saved. Continue...");
            }
            if(savedInstanceState.containsKey(COUNTER_KEY)) {
                Log.i(TAG, "Restore article counter from saved instance state");
                articleCounter = savedInstanceState.getInt(COUNTER_KEY);
            }
            if(savedInstanceState.containsKey(TEXT_VIEW_KEY)) {
                ((TextView)findViewById(R.id.text_info)).setText(savedInstanceState.getString(TEXT_VIEW_KEY));
                printMessage("Info TextView content was restored from saved bundle");
            }
        }

        printMessage("-- Activity lifecycle -- onCreate");
        Log.i(TAG, "TextView: " + ((TextView)findViewById(R.id.text_info)).getText().toString());

        LoaderManager loaderManager = getLoaderManager();
        Loader<List> newsLoader = loaderManager.getLoader(NEWS_LOADER_ID);
        if(newsLoader == null) {
            Log.i(TAG, "init NewsLoader");
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            Log.i(TAG, "restart NewsLoader");
            loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
        }
    }

    private void printMessage(String message) {
        Log.i(TAG, message);
        TextView tvInfo = findViewById(R.id.text_info);
        tvInfo.setText(String.format("%s\n%s", tvInfo.getText().toString(), message));
    }

    @Override
    protected void onStart() {
        super.onStart();
        printMessage("-- Activity lifecycle -- onStart");
        Log.i(TAG, "TextView content: \n\""
                + ((TextView)findViewById(R.id.text_info)).getText().toString()
                + "\"\n--------------\n");
    }

    @Override
    protected void onResume() {
        super.onResume();
        printMessage("-- Activity lifecycle -- onResume");
        Log.i(TAG, "TextView content: "
                + ((TextView)findViewById(R.id.text_info)).getText().toString());

    }

    @Override
    protected void onPause() {
        super.onPause();
        printMessage("-- Activity lifecycle -- onPause");
        Log.i(TAG, "TextView content: "
                + ((TextView)findViewById(R.id.text_info)).getText().toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
        printMessage("-- Activity lifecycle -- onStop");
        Log.i(TAG, "TextView content: "
                + ((TextView)findViewById(R.id.text_info)).getText().toString());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        printMessage("-- Activity lifecycle -- onRestart");
        Log.i(TAG, "TextView content: "
                + ((TextView)findViewById(R.id.text_info)).getText().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        printMessage("-- Activity lifecycle -- onDestroy");
        Log.i(TAG, "TextView content: "
                + ((TextView)findViewById(R.id.text_info)).getText().toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState - Article title: " + ((TextView)findViewById(R.id.article_title)).getText().toString());
        outState.putInt(COUNTER_KEY, articleCounter);
        outState.putSerializable(ARTICLE_KEY, articles.get(articleCounter));
        outState.putString(TEXT_VIEW_KEY, ((TextView)findViewById(R.id.text_info)).getText().toString());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Loader<ArrayList<ArticleEntity>> onCreateLoader(int id, @Nullable Bundle args) {
        Log.i(TAG, "'onCreateLoader' STARTED");
        printMessage("Create loader - Start loading ...");
        disableGUI();
        return new NewsLoader(this, NEWS_URI);
    }


    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<ArticleEntity>> loader, ArrayList<ArticleEntity> data) {
        Log.i(TAG, "onLoadFinished");
        Log.i(TAG, "RECEIVED DATA: " + data);
        articles = data;
        if(!isArticleRendered) {
            ArticleEntity article = switchArticle(data);
            renderArticle(article);
            isArticleRendered = true;
        }
        enableGUI();
        printMessage("Load finished.");
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<ArticleEntity>> loader) {
        Log.i(TAG, "onLoaderReset");
        printMessage("Reset loader - Start loading ...");
        disableGUI();
    }

    private void renderArticle(ArticleEntity entity) {
        TextView title = findViewById(R.id.article_title),
                content = findViewById(R.id.article_content);

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
    }

    private ArticleEntity switchArticle(List<ArticleEntity> articles) {
        if (articleCounter < 0)
            articleCounter = articles.size() - 1;
        else if (articleCounter > articles.size() - 1)
            articleCounter = 0;

        curArticle = articles.get(articleCounter);
        return curArticle;
    }

    void enableGUI() {
        Button prevButton = findViewById(R.id.btn_prev),
                nextButton = findViewById(R.id.btn_next);

        prevButton.setEnabled(true);
        nextButton.setEnabled(true);

        if(articles != null) {
            prevButton.setOnClickListener(btn -> {
                articleCounter--;
                renderArticle(switchArticle(articles));
            });

            nextButton.setOnClickListener(btn -> {
                articleCounter++;
                renderArticle(switchArticle(articles));
            });
        }
    }

    void disableGUI() {
        Button prevButton = findViewById(R.id.btn_prev),
                nextButton = findViewById(R.id.btn_next);

        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
    }
}
