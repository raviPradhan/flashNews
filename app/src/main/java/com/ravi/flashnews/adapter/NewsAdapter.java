package com.ravi.flashnews.adapter;

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

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private ArrayList<News> newsList;
    /*
     * An on-click handler that I have defined to make it easy for an Activity to interface with
     * movie list RecyclerView
     */
    private final ItemClickListener mClickHandler;

    public NewsAdapter(ArrayList<News> list, ItemClickListener clickHandler) {
        this.newsList = list;
        mClickHandler = clickHandler;
    }


    /**
     * Cache of the children views for a movie list item.
     */
    class NewsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_news_item_image)
        ImageView image;
        @BindView(R.id.tv_news_item_title)
        TextView title;
        @BindView(R.id.tv_news_item_source)
        TextView source;
        @BindView(R.id.tv_news_item_publishedDate)
        TextView publishedDate;

        NewsViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);

        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsViewHolder holder, int position) {
//        final int adapterPosition = holder.getAdapterPosition();
        final News currentItem = newsList.get(position);

        holder.title.setText(currentItem.getTitle());
        holder.publishedDate.setText(currentItem.getPublishedDate());
        holder.source.setText(currentItem.getSourceName());

        Picasso.get()
                .load(currentItem.getImageUrl())
                .into(holder.image);

        ViewCompat.setTransitionName(holder.image, currentItem.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickHandler.onItemClick(holder.getAdapterPosition(), currentItem, holder.image);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }
}
