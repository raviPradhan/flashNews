package com.ravi.flashnews.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.RemoteViews;

import com.ravi.flashnews.MainActivity;
import com.ravi.flashnews.R;
import com.ravi.flashnews.utils.JsonKeys;
import com.ravi.flashnews.utils.PreferenceUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class NewsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
//        Log.v(Constants.TAG, "Updating widget provider onUpdate()");
//        WidgetUpdateService.startActionUpdateNewsWidget(context);
//        Log.e(JsonKeys.TAG, "onUpdate Called on its own");
        updateNewsWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateNewsWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews rv = getNewsRemoteView(context);
//        Log.e(JsonKeys.TAG, "Widget updated");
        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    /**
     * Creates and returns the RemoteView to be displayed in the widget
     *
     * @param context The context
     * @return The RemoteViews for the ListView widget
     */
    private static RemoteViews getNewsRemoteView(final Context context) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.news_widget);
        final PreferenceUtils pref = new PreferenceUtils(context);
        Picasso.get()
                .load(pref.getData(JsonKeys.IMAGE_URL_KEY))
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        views.setImageViewBitmap(R.id.iv_widget_image, bitmap);
                        views.setTextViewText(R.id.tv_widget_title, pref.getData(JsonKeys.TITLE_KEY));
                        views.setTextViewText(R.id.tv_widget_date, pref.getData(JsonKeys.PUBLISHED_AT_KEY));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        views.setTextViewText(R.id.tv_widget_title, pref.getData(JsonKeys.TITLE_KEY));
                        views.setTextViewText(R.id.tv_widget_date, pref.getData(JsonKeys.PUBLISHED_AT_KEY));
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.rl_widget_parent, appPendingIntent);
        return views;
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
