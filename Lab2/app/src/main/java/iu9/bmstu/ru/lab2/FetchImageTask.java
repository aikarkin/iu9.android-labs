package iu9.bmstu.ru.lab2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class FetchImageTask extends AsyncTask<String, Void, Bitmap> {
    private Activity activity;
    private ImageView img;

    private static final String TAG = "FetchImageTask";

    public FetchImageTask(Activity activity, ImageView img) {
        this.activity = activity;
        this.img = img;
    }

    public FetchImageTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Uri imgUri = Uri.parse(urls[0]).buildUpon().build();
        String imgUrl = imgUri.toString();
        Bitmap iconBitmap = null;

        try {
            if (imgUrl != null) {
                Log.i(TAG, imgUrl);

                InputStream in = new java.net.URL(imgUrl).openStream();
                iconBitmap = BitmapFactory.decodeStream(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } finally {
            if(iconBitmap == null) {
                iconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.article_no_photo);
            }
        }

        return iconBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if(img != null) {
            img.setImageBitmap(bitmap);
        }
    }
}
