package com.ravi.flashnews.adapter;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.flashnews.R;
import com.ravi.flashnews.model.News;
import com.ravi.flashnews.utils.ItemClickListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_AUTHOR;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_DESCRIPTION;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_FULL_URL;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_IMAGE_PATH;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_PUBLISHED_DATE;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_SOURCE;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_TITLE;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private Cursor mCursor;
    private ItemClickListener callback;

    public FavoritesAdapter(ItemClickListener callback) {
        this.callback = callback;
    }

    // Inner class for creating ViewHolders
    class FavoritesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_news_item_image)
        ImageView image;
        @BindView(R.id.tv_news_item_title)
        TextView title;
        @BindView(R.id.tv_news_item_source)
        TextView source;
        @BindView(R.id.tv_news_item_publishedDate)
        TextView publishedDate;

        FavoritesViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);

        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FavoritesViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        final int adapterPosition = position;

        final News news = new News();
        news.setTitle(mCursor.getString(mCursor.getColumnIndex(COLUMN_TITLE)));
        news.setPublishedDate(mCursor.getString(mCursor.getColumnIndex(COLUMN_PUBLISHED_DATE)));
        news.setImageUrl(mCursor.getString(mCursor.getColumnIndex(COLUMN_IMAGE_PATH)));
        news.setFullUrl(mCursor.getString(mCursor.getColumnIndex(COLUMN_FULL_URL)));
        news.setDescription(mCursor.getString(mCursor.getColumnIndex(COLUMN_DESCRIPTION)));
        news.setAuthor(mCursor.getString(mCursor.getColumnIndex(COLUMN_AUTHOR)));
        news.setSourceName(mCursor.getString(mCursor.getColumnIndex(COLUMN_SOURCE)));

        holder.title.setText(news.getTitle());
        holder.publishedDate.setText(news.getPublishedDate());
        holder.source.setText(news.getSourceName());

        Picasso.get()
                .load(news.getImageUrl())
                .into(holder.image);

        ViewCompat.setTransitionName(holder.image, news.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(adapterPosition, news, holder.image);
            }
        });

        /*holder.title.setText(mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_TITLE)));
        holder.publishedDate.setText(mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_PUBLISHED_DATE)));
        holder.source.setText(mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_SOURCE)));

        Picasso.get()
                .load(mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_IMAGE_PATH)))
                .into(holder.image);

        ViewCompat.setTransitionName(holder.image,
                mCursor.getString(mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_TITLE)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(JsonKeys.TAG, "click position: " + adapterPosition + " title: " + mCursor.getString(mCursor.getColumnIndex(COLUMN_TITLE)));

                callback.onItemClick(holder.image, adapterPosition, mCursor);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }
}