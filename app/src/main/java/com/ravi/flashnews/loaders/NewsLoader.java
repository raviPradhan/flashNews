package com.ravi.flashnews.loaders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.ravi.flashnews.model.News;

import java.util.ArrayList;

public class NewsLoader extends AsyncTaskLoader<ArrayList<News>> {
    private ArrayList<News> resultList;
    private String url;

    public NewsLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        if (resultList != null)
            deliverResult(resultList);
        else
            forceLoad();
    }

    @Override
    public ArrayList<News> loadInBackground() {
        return BackgroundUtils.getDataFromBackend(url);
    }

    @Override
    public void deliverResult(@Nullable ArrayList<News> data) {
        resultList = data;
        super.deliverResult(data);
    }
}
