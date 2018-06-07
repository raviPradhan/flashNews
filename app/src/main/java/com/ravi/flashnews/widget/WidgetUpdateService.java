package com.ravi.flashnews.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

public class WidgetUpdateService extends IntentService {

    public static final String ACTION_UPDATE_WIDGET = "com.ravi.flashnews.action.update_widget";

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    /**
     * Starts this service to perform UpdatePlantWidgets action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateNewsWidget(Context context) {
//        Log.v(Constants.TAG, "startActionUpdateRecipeWidget()");
        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WIDGET.equals(action)) {
                handleActionUpdateWidget();
            }
        }
    }

    /**
     * Handle action UpdateWidget in the provided background thread
     */
    private void handleActionUpdateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, NewsWidgetProvider.class));
        NewsWidgetProvider.updateNewsWidgets(this, appWidgetManager, appWidgetIds);
    }
}
