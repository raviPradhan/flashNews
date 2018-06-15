package com.ravi.flashnews.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.ravi.flashnews.provider.FavoritesContract;

public class GetFavoritesLoader extends AsyncTaskLoader<Cursor> {

    // Initialize a Cursor, this will hold all the task data
    private Cursor mTaskData = null;
    private String selectionArgs, selection;

    public GetFavoritesLoader(Context context, String selectionArgs) {
        super(context);
        this.selectionArgs = selectionArgs;
        this.selection = null;
    }

    @Override
    protected void onStartLoading() {
        if (mTaskData != null) {
            // Delivers any previously loaded data immediately
            deliverResult(mTaskData);
        } else {
            // Force a new load
            forceLoad();
        }
    }

    // loadInBackground() performs asynchronous loading of data
    @Override
    public Cursor loadInBackground() {
        // Load data by querying
        try {
            if (selectionArgs != null) {
                selection = FavoritesContract.FavoritesEntry.COLUMN_TITLE + "=?";
            }
            return getContext().getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                    null,
                    selection,
                    (selectionArgs != null) ? new String[]{selectionArgs} : null,
                    null);

        } catch (Exception e) {
//            Log.e("FAVORITES LOADER", "Failed to asynchronously load data.");
//            e.printStackTrace();
            return null;
        }
    }

    // deliverResult sends the result of the load, a Cursor, to the registered listener
    public void deliverResult(Cursor data) {
        mTaskData = data;
        super.deliverResult(data);
    }
}
