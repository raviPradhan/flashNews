package com.ravi.flashnews;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.flashnews.loaders.GetFavoritesLoader;
import com.ravi.flashnews.model.News;
import com.ravi.flashnews.provider.FavoritesContract;
import com.ravi.flashnews.utils.JsonKeys;
import com.ravi.flashnews.utils.MessageUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_AUTHOR;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_DESCRIPTION;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_FULL_URL;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_IMAGE_PATH;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_PUBLISHED_DATE;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_SOURCE;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.COLUMN_TITLE;
import static com.ravi.flashnews.provider.FavoritesContract.FavoritesEntry.CONTENT_URI;

public class NewsDetailActivity extends AppCompatActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.cl_details)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_detail_image)
    ImageView image;
    @BindView(R.id.tv_detail_content)
    TextView content;
    @BindView(R.id.tv_detail_title)
    TextView title;
    @BindView(R.id.tv_detail_date)
    TextView publishedDate;
    @BindView(R.id.tv_detail_source)
    TextView source;
    @BindView(R.id.tv_detail_url)
    TextView fullStory;
    @BindView(R.id.fab_news_detail_favourite)
    FloatingActionButton favButton;

    private News newsItem;
    private boolean isFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        ButterKnife.bind(this);
        supportPostponeEnterTransition();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            newsItem = extras.getParcelable(JsonKeys.EXTRA_NEWS_ITEM);
            title.setText(newsItem.getTitle());
            content.setText(newsItem.getDescription());
            source.setText(getString(R.string.author, newsItem.getSourceName()));
            publishedDate.setText(newsItem.getPublishedDate());

            String imageUrl = newsItem.getImageUrl();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String imageTransitionName = extras.getString(JsonKeys.EXTRA_NEWS_IMAGE_TRANSITION_NAME);
                image.setTransitionName(imageTransitionName);
            }
            Picasso.get()
                    .load(imageUrl)
                    .noFade()
                    .into(image, new Callback() {
                        @Override
                        public void onError(Exception e) {
                            supportStartPostponedEnterTransition();
                            favButton.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onSuccess() {
                            supportStartPostponedEnterTransition();
                            favButton.setVisibility(View.VISIBLE);
                        }
                    });
            Spannable wordtoSpan = new SpannableString(getString(R.string.read_full));
            wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorIcons)),
                    0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            fullStory.setText(wordtoSpan);

            favButton.setOnClickListener(this);
            fullStory.setOnClickListener(this);
            getSupportLoaderManager().initLoader(100, null, this);
        } else {
            MessageUtils.showAlertDialogWithFinish(this, getString(R.string.details_error));
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new GetFavoritesLoader(this, newsItem.getTitle());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            favButton.setImageResource(R.drawable.ic_favorite_filled);
            isFavourite = true;
        } else {
            favButton.setImageResource(R.drawable.ic_favorite);
            isFavourite = false;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_detail_url:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(newsItem.getFullUrl()));
                startActivity(intent);
                break;

            case R.id.fab_news_detail_favourite:
                if (isFavourite) {
                    // Build appropriate uri with String row id appended to delete
                    String stringId = newsItem.getTitle();
                    Uri uri = FavoritesContract.FavoritesEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(stringId).build();
                    try {
                        if (getContentResolver().delete(uri, null, null) > 0) {
                            favButton.setImageResource(R.drawable.ic_favorite);
                            isFavourite = false;
                        } else {
                            MessageUtils.showSnackMessage(coordinatorLayout, getString(R.string.favourites_removal_error));
                        }
                    } catch (UnsupportedOperationException uex) {
                        uex.printStackTrace();
                        MessageUtils.showSnackMessage(coordinatorLayout, getString(R.string.favourites_removal_error));
                    }

                } else {
                    //            Adding news to favorites
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(COLUMN_TITLE, newsItem.getTitle());
                    contentValues.put(COLUMN_AUTHOR, newsItem.getAuthor());
                    contentValues.put(COLUMN_DESCRIPTION, newsItem.getDescription());
                    contentValues.put(COLUMN_SOURCE, newsItem.getSourceName());
                    contentValues.put(COLUMN_IMAGE_PATH, newsItem.getImageUrl());
                    contentValues.put(COLUMN_FULL_URL, newsItem.getFullUrl());
                    contentValues.put(COLUMN_PUBLISHED_DATE, newsItem.getPublishedDate());
                    try {
                        // Insert the content values via a ContentResolver
                        if (getContentResolver().insert(CONTENT_URI, contentValues) != null) {
                            favButton.setImageResource(R.drawable.ic_favorite_filled);
                            isFavourite = true;
                        } else {
                            MessageUtils.showSnackMessage(coordinatorLayout, getString(R.string.favourites_add_error));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        MessageUtils.showSnackMessage(coordinatorLayout, getString(R.string.favourites_add_error));
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.download));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        getString(R.string.concat_three_strings,
                                newsItem.getTitle(),
                                newsItem.getFullUrl(),
                                getString(R.string.share_text)));
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onBackPressed() {
        favButton.setVisibility(View.GONE);
        super.onBackPressed();
    }*/
}
