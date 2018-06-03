package com.ravi.flashnews.utils;

import android.database.Cursor;
import android.widget.ImageView;

public interface CursorItemClickListener {
    void onItemClick(ImageView imageView, int position, Cursor cursor);
}
