package com.ravi.flashnews;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.ravi.flashnews.adapter.FavoritesAdapter;
import com.ravi.flashnews.loaders.GetFavoritesLoader;
import com.ravi.flashnews.model.News;
import com.ravi.flashnews.utils.ItemClickListener;
import com.ravi.flashnews.utils.JsonKeys;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoritesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        ItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.rv_fav_newsRecycler)
    RecyclerView favRecycler;
    @BindView(R.id.pb_fav_progress)
    ProgressWheel progress;
    @BindView(R.id.tv_fav_emptyText)
    TextView emptyText;

    private FavoritesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favorites);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarTitle.setText(getString(R.string.favorites));

        favRecycler.setHasFixedSize(true);
        favRecycler.setLayoutManager(new LinearLayoutManager(this));
        try {
            if (savedInstanceState != null) {
                Log.e(JsonKeys.TAG, "restoring state");
                Parcelable state = savedInstanceState.getParcelable("rotation");
                favRecycler.getLayoutManager().onRestoreInstanceState(state);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        adapter = new FavoritesAdapter(this);
        favRecycler.setAdapter(adapter);

        progress.setVisibility(View.VISIBLE);
//        Log.e(JsonKeys.TAG, "Getting all favorites");
        getSupportLoaderManager().initLoader(100, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new GetFavoritesLoader(this, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        progress.setVisibility(View.GONE);
        if (data == null || data.getCount() <= 0) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.empty_favorites);
        } else {
            if (adapter == null) { // if adapter for favorites was not created before
//                Log.e(JsonKeys.TAG, "new adapter data");
                adapter.swapCursor(data);
            } else {
//                Log.e(JsonKeys.TAG, "refreshing data");
                adapter.swapCursor(data);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

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
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackAction();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable("rotation", favRecycler.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onBackPressed() {
        onBackAction();
        super.onBackPressed();
    }

    private void onBackAction() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
