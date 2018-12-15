package iu9.bmstu.ru.lab3;

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

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ArticleViewHolder> {
    private static final String TAG = "NewsListAdapter";

    private Context mContext;
    private List<ArticleEntity> entities;

    public NewsListAdapter(Context mContext, String newsUri) throws MalformedURLException, ExecutionException, InterruptedException {
        Log.i(TAG, "constructor");
        this.mContext = mContext;
        FetchNewsTask fetchTask = new FetchNewsTask();
        fetchTask.execute(new URL(newsUri));
        this.entities = fetchTask.get();
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
        ArticleEntity entity = entities.get(position);
        String imgUrl = entity.getImageUrl();

        Log.i(TAG, "bind view holder #" + String.valueOf(position) + ": " + entity.toString());

        if(imgUrl != null) {
            Log.i(TAG, "Load image: " + imgUrl);

            RequestOptions glideOpt = new RequestOptions()
                    .centerCrop()
                    .error(R.drawable.item_nophoto);

            Glide.with(mContext)
                    .asBitmap()
                    .load(entity.getImageUrl())
                    .apply(glideOpt)
                    .into(holder.image);
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
