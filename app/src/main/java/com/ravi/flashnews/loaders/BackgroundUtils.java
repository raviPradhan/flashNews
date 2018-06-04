package com.ravi.flashnews.loaders;

import android.util.Log;

import com.ravi.flashnews.model.News;
import com.ravi.flashnews.utils.HelperFunctions;
import com.ravi.flashnews.utils.JsonKeys;
import com.ravi.flashnews.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class BackgroundUtils {

    public static ArrayList<News> getDataFromBackend(String url){
        try {
            String response = NetworkUtils.getJson(url);
            Log.e(JsonKeys.TAG, response);
            JSONObject jsonResponse = new JSONObject(response);
            ArrayList<News> list = new ArrayList<>();
            if (HelperFunctions.isValidResponse(jsonResponse)) {
                JSONArray dataArray = jsonResponse.getJSONArray(JsonKeys.ARTICLES_KEY);
                for (int i = 0; i < dataArray.length(); i++) {
                    News news = new News();
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    news.setSourceName(dataObject.getJSONObject(JsonKeys.SOURCE_KEY).getString(JsonKeys.NAME_KEY));
                    news.setTitle(dataObject.getString(JsonKeys.TITLE_KEY));
                    news.setAuthor(dataObject.getString(JsonKeys.AUTHOR_KEY));
                    news.setDescription(dataObject.getString(JsonKeys.DESCRIPTION_KEY));
                    news.setFullUrl(dataObject.getString(JsonKeys.URL_KEY));
                    news.setImageUrl(dataObject.getString(JsonKeys.IMAGE_URL_KEY));
                    news.setPublishedDate(HelperFunctions.getViewableDate(dataObject.getString(JsonKeys.PUBLISHED_AT_KEY)));
                    list.add(news);
                }
                return list;
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } catch (JSONException jex) {
            jex.printStackTrace();
        }
        return null;
    }
}
