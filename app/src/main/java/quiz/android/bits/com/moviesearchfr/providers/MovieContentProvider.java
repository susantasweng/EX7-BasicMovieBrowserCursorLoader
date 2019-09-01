package quiz.android.bits.com.moviesearchfr.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

import quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper;

public class MovieContentProvider extends ContentProvider {

    private MovieDatabaseHelper databaseHelper;
    private Context context;
    private static final int ALL_MOVIES = 1;
    private static final int SINGLE_MOVIE = 2;

    private static final String AUTHORITY = "quiz.android.bits.com.movie.provider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/all");
    public static final Uri SINGLE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/single");
    public static final UriMatcher uriMatcher;
    private SQLiteDatabase db;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "all", ALL_MOVIES);
        uriMatcher.addURI(AUTHORITY, "single/#", SINGLE_MOVIE);
    }

    @Override
    public boolean onCreate() {
        context = getContext();
        databaseHelper = new MovieDatabaseHelper(context);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_MOVIES: return "vnd.android.cursor.dir/vnd.paad.movie";
            case SINGLE_MOVIE: return "vnd.android.cursor.item/vnd.paad.movie";
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        db = databaseHelper.getWritableDatabase();
        List<String> segments = uri.getPathSegments();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(MovieDatabaseHelper.TABLE_NAME);

        // If this is a raw query, limit the result set to the passed in row
        switch (uriMatcher.match(uri)) {
            case SINGLE_MOVIE: queryBuilder.appendWhere(MovieDatabaseHelper.COLUMN_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                break;
        }

        String orderBy;
        if(TextUtils.isEmpty(sortOrder)) {
            orderBy = MovieDatabaseHelper.COLUMN_TITLE;
        } else {
            orderBy = sortOrder;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Register the Contexts content resolver to be notified if the cursor result set changes.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri _uri, ContentValues values) {
        long rowId = db.insert(MovieDatabaseHelper.TABLE_NAME, "movie", values);
        if(rowId > 0) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            notifyChangeToResolver(uri);
            return uri;
        }

        throw new SQLException("Failed to insert row into" + _uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        selection = selectionFromUri(uri, selection);

        // To return the number of deleted items you must specify a where
        // clause. To delete all rows and return a value pass in “1”.
        if(selection == null) {
            selection = "1";
        }

        // Perform the deletion.
        int deleteCount = db.delete(MovieDatabaseHelper.TABLE_NAME, selection, selectionArgs);
        notifyChangeToResolver(uri);
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        selection = selectionFromUri(uri, selection);

        int updateCount = db.update(MovieDatabaseHelper.TABLE_NAME,values, selection, selectionArgs);
        notifyChangeToResolver(uri);
        return updateCount;
    }

    private void notifyChangeToResolver(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    // if the Uri talks about single row, our operation should impact only one row not to all.
    private String selectionFromUri(Uri uri, String selection) {
        switch(uriMatcher.match(uri)) {
            case SINGLE_MOVIE:
                    String row_id = uri.getPathSegments().get(1);
                    selection = MovieDatabaseHelper.COLUMN_ID + "=" + row_id
                            + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                break;

                default: break;
        }

        return selection;
    }
}
