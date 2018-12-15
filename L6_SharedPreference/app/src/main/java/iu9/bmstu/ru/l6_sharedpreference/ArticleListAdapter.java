package iu9.bmstu.ru.l6_sharedpreference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ArticleViewHolder>  {
    private static final String TAG = "ArticleListAdapter";
    private final float DP_COEF;

    private Context mContext;
    private List<Article> entities;
    private String newsUri;
    private boolean picturesShown;

    public ArticleListAdapter(Context mContext, String newsUri, boolean mustPicturesShown) throws MalformedURLException, ExecutionException, InterruptedException {
        Log.i(TAG, "ArticleListAdapter: newsUri: " + newsUri);
        this.mContext = mContext;
        this.newsUri = newsUri;
        DP_COEF = mContext.getResources().getDisplayMetrics().density;
        this.picturesShown = mustPicturesShown;
        ArticleFetchingTask fetchTask = new ArticleFetchingTask();
        fetchTask.execute(new URL(this.newsUri));
        this.entities = fetchTask.get();
        Log.i(TAG, "ArticleListAdapter: entities=" + entities);
    }

    public String getNewsUri() {
        return newsUri;
    }

    public void setNewsUri(String newsUri) {
        this.newsUri = newsUri;
    }

    public boolean isPicturesShown() {
        return picturesShown;
    }

    public void setPicturesShown(boolean picturesShown) {
        this.picturesShown = picturesShown;
    }

    public void refresh() throws MalformedURLException, ExecutionException, InterruptedException {
        ArticleFetchingTask fetchTask = new ArticleFetchingTask();
        fetchTask.execute(new URL(this.newsUri));
        this.entities = fetchTask.get();
        this.notifyDataSetChanged();

        Log.i(TAG, "Got entities: " + entities.toString());
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "create view holder");
        Context ctx = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.layout_listitem, parent, false);

        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        final int imgLen = (int)(80 * DP_COEF);
        Article entity = entities.get(position);
        String imgUrl = entity.getImageUrl();

        Log.i(TAG, "bind view holder #" + String.valueOf(position) + ": " + entity.toString());

        if(picturesShown && imgUrl != null) {
            Log.i(TAG, "Load image: " + imgUrl);
            holder.image.setVisibility(View.VISIBLE);
            holder.image.setLayoutParams(new RelativeLayout.LayoutParams(imgLen, imgLen));

            RequestOptions glideOpt = new RequestOptions()
                    .centerCrop()
                    .error(R.drawable.item_nophoto);

            Glide.with(mContext)
                    .asBitmap()
                    .load(entity.getImageUrl())
                    .apply(glideOpt)
                    .into(holder.image);
        } else {
            holder.image.setVisibility(View.INVISIBLE);
            holder.image.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
        }

        holder.title.setText(entity.getTitle());
        holder.description.setText(entity.getDescription());

        String articleUrl = entity.getUrl();

        if(articleUrl != null ) {
            holder.itemLayout.setOnClickListener((View v) -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                mContext.startActivity(browserIntent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "NewsListAdapter.ArticleViewHolder";
        ImageView image;
        TextView title;
        TextView description;
        RelativeLayout itemLayout;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            Log.i(TAG, "constructor");

            image = itemView.findViewById(R.id.item_image);
            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            itemLayout = itemView.findViewById(R.id.item_layout);
        }
    }
}
