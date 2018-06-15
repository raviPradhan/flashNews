package com.ravi.flashnews;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.ravi.flashnews.adapter.NewsAdapter;
import com.ravi.flashnews.background.NewsJobService;
import com.ravi.flashnews.loaders.NewsLoader;
import com.ravi.flashnews.model.News;
import com.ravi.flashnews.utils.ItemClickListener;
import com.ravi.flashnews.utils.JsonKeys;
import com.ravi.flashnews.utils.MessageUtils;
import com.ravi.flashnews.utils.NetworkUtils;
import com.ravi.flashnews.utils.PreferenceUtils;
import com.ravi.flashnews.widget.NewsWidgetProvider;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<News>>,
        ItemClickListener, SwipeRefreshLayout.OnRefreshListener, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.rv_main_newsRecycler)
    RecyclerView newsRecycler;
    @BindView(R.id.srl_main_refreshLayout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.pb_main_progress)
    ProgressWheel progress;
    @BindView(R.id.tv_main_emptyText)
    TextView emptyText;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_toolbar_title)
    TextView toolbarTitle;

    private ArrayList<News> newsList;
    private NewsAdapter adapter;
    private GoogleApiClient mGoogleApiClient;
    private boolean isClientConnected = true;

    private static final int JOB_INTERVAL_MINUTES = 3;
    private static final int JOB_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(JOB_INTERVAL_MINUTES));
    private static final int SYNC_FLEXTIME_SECONDS = JOB_INTERVAL_SECONDS;
    private static final String NEWS_JOB_TAG = "news_tag";

    private InterstitialAd mInterstitialAd;
    private PreferenceUtils preferenceUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbarTitle.setText(getString(R.string.app_name));

        preferenceUtils = new PreferenceUtils(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        newsList = new ArrayList<>();
        newsRecycler.setHasFixedSize(true);
        newsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(newsList, this);
        newsRecycler.setAdapter(adapter);

        if (getIntent().getExtras() != null) {
            // if news list is set in extras
            // mostly coming from notification
            newsList.addAll(getIntent().<News>getParcelableArrayListExtra(JsonKeys.ARTICLES_KEY));
            adapter.notifyDataSetChanged();
        } else {
            progress.setVisibility(View.VISIBLE);
        }
        startLoader();
        refreshLayout.setOnRefreshListener(this);

        loadAd();
        startJob();
    }

    private void startJob() {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(NewsJobService.class)
                // uniquely identifies the job
                .setTag(NEWS_JOB_TAG)
                /*
                 * Network constraints on which this Job should run. In this app, we're using the
                 * ON_ANY_NETWORK constraint so that the job executes if the device is
                 * connected to any network.
                 *
                 * In a normal app, it might be a good idea to include a preference for this,
                 * as different users may have different preferences on when you should be
                 * syncing your application's data.
                 */
                .setConstraints(
                        //run on any network
                        Constraint.ON_ANY_NETWORK
                )
                /*
                 * We want this to continuously happen, so we tell this Job to recur.
                 */
                .setRecurring(true)
                /*
                 * setLifetime sets how long this job should persist. The options are to keep the
                 * Job "forever" or to have it die the next time the device boots up.
                 */
                .setLifetime(Lifetime.FOREVER)
                /*
                 * We want the job to happen every 15 minutes or so. The first argument for
                 * Trigger class's static executionWindow method is the start of the time frame
                 * when the
                 * job should be performed. The second argument is the latest point in time at
                 * which the data should be synced. Please note that this end time is not
                 * guaranteed, but is more of a guideline for FirebaseJobDispatcher to go off of.
                 */
                .setTrigger(Trigger.executionWindow(JOB_INTERVAL_SECONDS,
                        JOB_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                /*
                 * If a Job with the tag with provided already exists, this new job will replace
                 * the old one.
                 */
                .setReplaceCurrent(true)
                /* Once the Job is ready, call the builder's build method to return the Job */
                .build();
        /* Schedule the Job with the dispatcher */
        dispatcher.schedule(myJob);
    }

    @Override
    public void onRefresh() {
        if (newsList.isEmpty())
            progress.setVisibility(View.VISIBLE);

        refreshLayout.setRefreshing(false);
        startLoader();
    }

    private void startLoader() {
        emptyText.setVisibility(View.GONE);
        if (NetworkUtils.isInternetConnected(this)) {
            getSupportLoaderManager().restartLoader(1, getBundle(), this);
        } else {
            progress.setVisibility(View.GONE);
            showMessage(getString(R.string.no_internet));
        }
    }

    private void showMessage(String message) {
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }

    private Bundle getBundle() {
        Bundle bundle = new Bundle();
        Uri builtUri = Uri.parse("https://newsapi.org/v2/top-headlines")
                .buildUpon()
                .appendQueryParameter(JsonKeys.COUNTRY_KEY, "in")
                .appendQueryParameter(JsonKeys.CATEGORY_KEY, "business")
                .appendQueryParameter(JsonKeys.API_KEY, BuildConfig.API_KEY)
                .build();
        bundle.putString(JsonKeys.URL_KEY, builtUri.toString());
        return bundle;
    }

    @NonNull
    @Override
    public Loader<ArrayList<News>> onCreateLoader(int id, @Nullable Bundle args) {
        if (args != null) {
            String urlString = args.getString(JsonKeys.URL_KEY);
            return new NewsLoader(this, urlString);
        }
        throw new UnsupportedOperationException("The Url is not provided");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<News>> loader, ArrayList<News> data) {
        progress.setVisibility(View.GONE);
        if (data != null) {
            if (data.isEmpty()) {
                showMessage(getString(R.string.empty_news));
            } else {
                newsList.clear();
                newsList.addAll(data);
                adapter.notifyDataSetChanged();
                // update widget data
                updateWidget(newsList.get(0));
            }
        } else {
            showMessage(getString(R.string.server_error));
        }
    }

    private void updateWidget(News item) {
        // update the news in the widget
        preferenceUtils.setData(JsonKeys.TITLE_KEY, item.getTitle());
        preferenceUtils.setData(JsonKeys.PUBLISHED_AT_KEY, item.getPublishedDate());
        preferenceUtils.setData(JsonKeys.IMAGE_URL_KEY, item.getImageUrl());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.
                getAppWidgetIds(
                        new ComponentName(this, NewsWidgetProvider.class));
        Log.e(JsonKeys.TAG, "updating widget from main activity");
        NewsWidgetProvider.updateNewsWidgets(this, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<News>> loader) {

    }

    private void loadAd() {
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(BuildConfig.INTERSTITIAL_AD_KEY);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.e(JsonKeys.TAG, "Ad has loaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.e(JsonKeys.TAG, "AD ERROR " + i);
            }
        });
    }

    @Override
    public void onItemClick(int position, News news, ImageView imageView) {
        Intent intent = new Intent(this, NewsDetailActivity.class);
        intent.putExtra(JsonKeys.EXTRA_NEWS_ITEM, news);
        intent.putExtra(JsonKeys.EXTRA_NEWS_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                imageView,
                ViewCompat.getTransitionName(imageView));

        startActivity(intent, options.toBundle());
    }

    @Override
    protected void onResume() {
        super.onResume();
        int count = preferenceUtils.getIntData(JsonKeys.TO_SHOW_AD);
        // increment the number of times the app has opened its main activity
        preferenceUtils.setData(JsonKeys.TO_SHOW_AD, ++count);
        Log.e(JsonKeys.TAG, "" + count);
        /* this shows ad after every 5 resumes of the app in every resume.
         * This logic can be changed any time accordingly
         * */
        if (count % 5 == 0) {
            showAd();
        }
    }

    private void showAd() {
        // show add if loaded
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            MessageUtils.showToast(this, "The advertisement could not be loaded.");
            loadAd();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                MessageUtils.showCustomDialog(this,
                        getString(R.string.logout_label),
                        getString(R.string.yes),
                        getString(R.string.not_now),
                        new MessageUtils.DoubleDialogCallback() {
                            @Override
                            public void onOk(Context context) {
                                if (isClientConnected) {
                                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                            new ResultCallback<Status>() {
                                                @Override
                                                public void onResult(@NonNull Status status) {
                                                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            });
                                } else {
                                    MessageUtils.showAlertDialog(context, getString(R.string.google_error));
                                }
                            }

                            @Override
                            public void onCancel(Context context) {
                                // do nothing, dialog will be dismissed on its own
                            }
                        });
                break;

            case R.id.action_favorites:
                startActivity(new Intent(this, FavoritesActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        isClientConnected = false;
    }
}
