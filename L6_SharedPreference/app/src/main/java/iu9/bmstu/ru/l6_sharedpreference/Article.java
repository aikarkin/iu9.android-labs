package iu9.bmstu.ru.l6_sharedpreference;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class Article {
    private String imageUrl;
    private String articleLink;
    private String title;
    private String source;
    private String author;
    private String description;
    private String content;
    private Date published;

    private static final String TAG = "Article";

    public String getImageUrl() {
        return imageUrl;
    }

    public Date getPublished() {
        return published;
    }


    public String getTitle() {
        return title;
    }


    public String getSource() {
        return source;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return articleLink;
    }

    public static List<Article> parseEntitiesFromJson(String content) throws JSONException, ParseException {
        List<Article> entities = new ArrayList<>();
        JSONObject respJson = new JSONObject(content);
        JSONArray newsArr = respJson.getJSONArray("articles");

        for (int i = 0; i < newsArr.length(); i++) {
            SimpleDateFormat articleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

            JSONObject article = newsArr.getJSONObject(i);
            Article entity = new Article();

            try {
                entity.author = article.getString("author");
                entity.title = article.getString("title");
                entity.imageUrl = article.getString("urlToImage");
                entity.articleLink = article.getString("url");
                entity.published = articleDateFormat.parse(article.getString("publishedAt"));
                entity.source = article.getJSONObject("source").getString("name");
                entity.description = article.getString("description");
                entity.content = article.getString("content");

                entities.add(entity);
            } catch (JSONException | ParseException e) {
                Log.i(TAG, "parseEntitiesFromJson: failed to parse entity: " + article.toString() + ". Error: " + e.getMessage());
            }
        }

        return entities;
    }

    @Override
    public String toString() {
        return this.title;
    }

    public String getContent() {
        return content;
    }
}
