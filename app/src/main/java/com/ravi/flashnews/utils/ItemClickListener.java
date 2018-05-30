package com.ravi.flashnews.utils;

import android.widget.ImageView;

import com.ravi.flashnews.model.News;

public interface ItemClickListener {
    void onItemClick(int position, News newsItem, ImageView imageView);
}
