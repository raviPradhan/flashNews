package com.ravi.flashnews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.ravi.flashnews.adapter.NewsAdapter;
import com.ravi.flashnews.model.News;
import com.ravi.flashnews.utils.HelperFunctions;
import com.ravi.flashnews.utils.ItemClickListener;
import com.ravi.flashnews.utils.JsonKeys;
import com.ravi.flashnews.utils.MessageUtils;
import com.ravi.flashnews.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbarTitle.setText(getString(R.string.app_name));

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

        progress.setVisibility(View.VISIBLE);
        startLoader();

        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(false);
        startLoader();
    }

    private void startLoader() {
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
            }
        } else {
            showMessage(getString(R.string.server_error));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<News>> loader) {

    }

    private static class NewsLoader extends AsyncTaskLoader<ArrayList<News>> {
        private ArrayList<News> resultList;
        private String url;

        NewsLoader(Context context, String url) {
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

        @Override
        public void deliverResult(@Nullable ArrayList<News> data) {
            resultList = data;
            super.deliverResult(data);
        }
    }

    @Override
    public void onItemClick(int position, News newsItem, ImageView imageView) {
        Intent intent = new Intent(this, NewsDetailActivity.class);
        intent.putExtra(JsonKeys.EXTRA_NEWS_ITEM, newsItem);
        intent.putExtra(JsonKeys.EXTRA_NEWS_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                imageView,
                ViewCompat.getTransitionName(imageView));

        startActivity(intent, options.toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
