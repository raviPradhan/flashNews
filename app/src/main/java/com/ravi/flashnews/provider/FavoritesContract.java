package com.ravi.flashnews.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoritesContract {

    static final String AUTHORITY = "com.ravi.flashnews";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    static final String PATH_FAVORITES = "favorites";

    public static final class FavoritesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        static final String TABLE_NAME = "favorites";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE_PATH = "image";
        public static final String COLUMN_DESCRIPTION = "synopsis";
        public static final String COLUMN_SOURCE = "source";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_PUBLISHED_DATE = "publishedDate";
        public static final String COLUMN_FULL_URL = "fullUrl";
    }
}
