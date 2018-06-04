package com.ravi.flashnews.loaders;

import android.net.Uri;
import android.os.AsyncTask;

import com.ravi.flashnews.BuildConfig;
import com.ravi.flashnews.background.NewsJobService;
import com.ravi.flashnews.model.News;
import com.ravi.flashnews.utils.JsonKeys;

import java.util.ArrayList;

public class BackgroundAsyncTask extends AsyncTask<Void, Void, News> {

    private NewsJobService.JobCallback callback;

    public BackgroundAsyncTask(NewsJobService.JobCallback callback) {
        this.callback = callback;
    }

    @Override
    protected News doInBackground(Void... voids) {
        Uri builtUri = Uri.parse("https://newsapi.org/v2/top-headlines")
                .buildUpon()
                .appendQueryParameter(JsonKeys.COUNTRY_KEY, "in")
                .appendQueryParameter(JsonKeys.CATEGORY_KEY, "business")
                .appendQueryParameter(JsonKeys.API_KEY, BuildConfig.API_KEY)
                .build();

        ArrayList<News> freshNews = BackgroundUtils.getDataFromBackend(builtUri.toString());
        if (freshNews != null && !freshNews.isEmpty())
            return freshNews.get(0);

        return null;
    }

    @Override
    protected void onPostExecute(News news) {
        super.onPostExecute(news);

        callback.resultCallback(news);
    }
}
