package com.ravi.flashnews;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the AdMob app
        MobileAds.initialize(this, BuildConfig.AD_MOB_KEY);
    }
}
