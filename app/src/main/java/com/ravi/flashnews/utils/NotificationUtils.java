package com.ravi.flashnews.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.ravi.flashnews.MainActivity;
import com.ravi.flashnews.R;
import com.ravi.flashnews.model.News;
import com.ravi.flashnews.widget.WidgetUpdateService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class NotificationUtils {

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 1138 is in no way significant.
     */
    private static final int NEWS_NOTIFICATION_ID = 1138;
    private static final String NOTIFICATION_CHANNEL_ID = "NEWS_CH_ID";

    public static void generateNotificationLayout(final Context context, ArrayList<News> newsList) {
        if (newsList != null) {

            Intent intent = new Intent(context, MainActivity.class);
            // pass the news list to the main activity when notification is clicked
            intent.putExtra(JsonKeys.ARTICLES_KEY, newsList);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            // Create the pending intent to launch the activity
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            final RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
            final News news = newsList.get(0);
            Picasso.get()
                    .load(news.getImageUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            contentView.setImageViewBitmap(R.id.iv_notification_image, bitmap);
                            contentView.setTextViewText(R.id.tv_notification_title, news.getTitle());
                            contentView.setTextViewText(R.id.tv_notification_date, news.getPublishedDate());
                            generateNotification(context, contentView, pendingIntent);
                            //TODO: Save the first news coming here in preferences in order to show the news in the widget
                            WidgetUpdateService.startActionUpdateNewsWidget(context);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            generateNotification(context, contentView, pendingIntent);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        } else {
            Log.e(JsonKeys.TAG, "Did not receive any news");
        }
    }

    private static void generateNotification(Context context, RemoteViews customView, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e(JsonKeys.TAG, "Notification generation initiated");
        // The user-visible name of the channel.
        CharSequence name = context.getString(R.string.app_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_notification)
                .setCustomContentView(customView)
                .setCustomBigContentView(customView)
                .setContent(customView)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        /* NEWS_NOTIFICATION_ID allows you to update or cancel the notification later on */
        notificationManager.notify(NEWS_NOTIFICATION_ID, notificationBuilder.build());
    }
}
