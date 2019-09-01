package quiz.android.bits.com.moviesearchfr.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import quiz.android.bits.com.moviesearchfr.models.MovieItem;

public class MovieDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "movies_db";

    public static final String TABLE_NAME = "movies";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CAST = "casts";
    public static final String COLUMN_DIRECTOR = "director";
    public static final String COLUMN_PLOT = "plot";
    public static final String COLUMN_THUMB = "thumbnail";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_CAST + " TEXT,"
                    + COLUMN_DIRECTOR + " TEXT,"
                    + COLUMN_PLOT + " TEXT,"
                    + COLUMN_THUMB + " TEXT"
                    + ")";


    public MovieDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create movies table
        db.execSQL(CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public boolean addMovie(MovieItem movieItem) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getContentValueFromItem(movieItem);
        long rowId = db.insert(TABLE_NAME, null, values);
        db.close();
        return rowId > 0;
    }

    public boolean updateItem(MovieItem movieItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getContentValueFromItem(movieItem);
        String whereClause = COLUMN_ID + " = ?";
        String whereArgs[] = new String[]{String.valueOf(movieItem.getId())};
        int rowId =  db.update(TABLE_NAME, values, whereClause, whereArgs);
        db.close();
        return rowId > 0;
    }

    public boolean deleteItem(MovieItem movieItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String whereArgs[] = new String[]{String.valueOf(movieItem.getId())};
        long rowId = db.delete(TABLE_NAME, whereClause, whereArgs);
        db.close();
        return rowId > 0;

    }

    private ContentValues getContentValueFromItem(MovieItem movieItem) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, movieItem.getId());
        values.put(COLUMN_TITLE, movieItem.getTitle());
        values.put(COLUMN_CAST, movieItem.getCast());
        values.put(COLUMN_DIRECTOR, movieItem.getDirector());
        values.put(COLUMN_PLOT, movieItem.getPlot());
        values.put(COLUMN_THUMB, movieItem.getThumbnail());

        return values;
    }

    public MovieItem getItem(int id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_CAST, COLUMN_DIRECTOR, COLUMN_PLOT},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare MovieItem object
        MovieItem movieItem = new MovieItem(
                cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_CAST)),
                cursor.getString(cursor.getColumnIndex(COLUMN_DIRECTOR)),
                cursor.getString(cursor.getColumnIndex(COLUMN_PLOT)),
                cursor.getString(cursor.getColumnIndex(COLUMN_THUMB)));

        // close the db connection
        cursor.close();

        return movieItem;
    }

    public List<MovieItem> getAllItems() {
        List<MovieItem> movieItems = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " +
                COLUMN_TITLE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                MovieItem movieItem = new MovieItem();
                movieItem.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                movieItem.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                movieItem.setCast(cursor.getString(cursor.getColumnIndex(COLUMN_CAST)));
                movieItem.setDirector(cursor.getString(cursor.getColumnIndex(COLUMN_DIRECTOR)));
                movieItem.setPlot(cursor.getString(cursor.getColumnIndex(COLUMN_PLOT)));
                movieItem.setThumbnail(cursor.getString(cursor.getColumnIndex(COLUMN_THUMB)));

                movieItems.add(movieItem);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return movieItems list
        return movieItems;
    }

    public int getItemsCount() {
        String countQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }
}
