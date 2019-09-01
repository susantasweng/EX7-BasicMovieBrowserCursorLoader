package quiz.android.bits.com.moviesearchfr.background;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_ID;
import static quiz.android.bits.com.moviesearchfr.providers.MovieContentProvider.CONTENT_URI;

public class StorageHandlerThread extends HandlerThread {

    public static final int QUERY_OP = 01;
    public static final int BULK_INSERT_OP = 02;
    public static final int INSERT_OP = 03;
    public static final int UPDATE_OP = 04;
    public static final int DELETE_OP = 05;

    private Context context;
    //private WeakReference<CustomHandler> handlerWeakReference;

    public StorageHandlerThread(Context context, String name) {
        super(name);
        this.context = context;
        //handlerWeakReference = :-);
    }

    public void bulkInsert(Uri contentUri, ContentValues[] allMovieContentValues) {
        Message msg = new Message();
        msg.what = BULK_INSERT_OP;
        msg.obj = allMovieContentValues;
        new WeakReference<CustomHandler>(new CustomHandler(context, getLooper(), null)).get().sendMessage(msg);
    }

    public void query(OnCallBack onCallBack) {
        Message msg = new Message();
        msg.what = QUERY_OP;
        new WeakReference<CustomHandler>(new CustomHandler(context, getLooper(), onCallBack)).get().sendMessage(msg);
    }

    public void insert(ContentValues contentValues, OnCallBack onCallBack) {
        Message msg = new Message();
        msg.what = INSERT_OP;
        msg.obj = contentValues;
        new WeakReference<CustomHandler>(new CustomHandler(context, getLooper(), null)).get().sendMessage(msg);
    }

    public void update(ContentValues contentValues, int id, OnCallBack onCallBack) {
        Message msg = new Message();
        msg.what = UPDATE_OP;
        msg.arg1 = id;
        msg.obj = contentValues;
        new WeakReference<CustomHandler>(new CustomHandler(context, getLooper(), null)).get().sendMessage(msg);
    }

    public void delete(int id, OnCallBack onCallBack) {
        Message msg = new Message();
        msg.what = DELETE_OP;
        msg.arg1 = id;
        new WeakReference<CustomHandler>(new CustomHandler(context, getLooper(), null)).get().sendMessage(msg);
    }

    public static class CustomHandler extends Handler {
        private Context context;
        private OnCallBack onCallBack;

        public CustomHandler(Context context, Looper looper, OnCallBack onCallBack) {
            super(looper);
            this.context = context;
            this.onCallBack = onCallBack;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StorageHandlerThread.BULK_INSERT_OP:
                    context.getContentResolver().bulkInsert(CONTENT_URI, (ContentValues[])msg.obj);
                    break;

                case StorageHandlerThread.QUERY_OP:
                    Cursor cursor = context.getContentResolver().query(CONTENT_URI, null, null, null, null);
                    if(onCallBack != null) {
                        onCallBack.onCallBack(cursor, msg.what, true);
                    }
                    break;

                case StorageHandlerThread.INSERT_OP:
                    Uri uri = context.getContentResolver().insert(CONTENT_URI, (ContentValues)msg.obj);
                    break;

                case StorageHandlerThread.UPDATE_OP:
                    int countOfRows = context.getContentResolver().update(CONTENT_URI,
                            (ContentValues)msg.obj, COLUMN_ID + " = ?", new String[]{msg.arg1 + ""});
                    if(onCallBack != null) {
                        onCallBack.onCallBack(null, msg.what, countOfRows > 0);
                    }
                    break;

                case StorageHandlerThread.DELETE_OP:
                    int _countOfRows = context.getContentResolver().delete(CONTENT_URI,
                            COLUMN_ID + " = ?", new String[]{msg.arg1 + ""});

                    if(onCallBack != null) {
                        onCallBack.onCallBack(null, msg.what, _countOfRows > 0);
                    }
                    break;
            }
        }
    }

    public interface OnCallBack {
        public void onCallBack(Cursor cursor, int what, boolean success);
    }
}
