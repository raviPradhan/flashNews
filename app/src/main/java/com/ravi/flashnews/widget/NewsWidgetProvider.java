package com.ravi.flashnews.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ravi.flashnews.MainActivity;
import com.ravi.flashnews.R;
import com.ravi.flashnews.utils.JsonKeys;
import com.ravi.flashnews.utils.PreferenceUtils;

public class NewsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI
//        Log.v(Constants.TAG, "Updating widget provider onUpdate()");
        WidgetUpdateService.startActionUpdateRecipeWidget(context);
    }

    static void updateNewsWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
//        Log.v(Constants.TAG, "updateAppWidget()");
        RemoteViews rv = getRecipeListRemoteView(context);
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_widget_ingredientsList);
    }

    /**
     * Creates and returns the RemoteViews to be displayed in the ListView widget
     *
     * @param context The context
     * @return The RemoteViews for the ListView widget
     */
    private static RemoteViews getRecipeListRemoteView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.news_widget);
        // Set the ListWidgetService intent to act as the adapter for the ListView
        PreferenceUtils pref = new PreferenceUtils(context);
        views.setTextViewText(R.id.tv_widget_title, pref.getStringData(JsonKeys.TITLE_KEY));
        views.setTextViewText(R.id.tv_widget_date, pref.getStringData(JsonKeys.PUBLISHED_AT_KEY));
        Intent intent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.lv_widget_ingredientsList, intent);
        // Set the PendingIntent template in getRecipeListRemoteView to launch MainActivity
        // Set the MainActivity intent to launch when clicked
        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.lv_widget_ingredientsList, appPendingIntent);
        // Handle empty recipe
        views.setEmptyView(R.id.lv_widget_ingredientsList, R.id.empty_view);
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
