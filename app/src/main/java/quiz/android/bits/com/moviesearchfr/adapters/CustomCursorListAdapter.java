package quiz.android.bits.com.moviesearchfr.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import quiz.android.bits.com.moviesearchfr.R;

import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_CAST;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_PLOT;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_THUMB;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_TITLE;

public class CustomCursorListAdapter extends CursorRecyclerViewAdapter {

    protected Context mContext;

    public class NewViewHolder extends RecyclerView.ViewHolder {
        public TextView title, plot, cast;
        public ImageView thumbnail;

        public NewViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            cast = view.findViewById(R.id.cast);
            plot = view.findViewById(R.id.plot);
            thumbnail = view.findViewById(R.id.thumbnail);
        }
    }

    public CustomCursorListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public NewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_layout, parent, false);

        return new NewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        NewViewHolder holder = ((NewViewHolder)viewHolder);
        cursor.moveToPosition(cursor.getPosition());
        holder.title.setText(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        holder.cast.setText(cursor.getString(cursor.getColumnIndex(COLUMN_CAST)));
        holder.plot.setText(cursor.getString(cursor.getColumnIndex(COLUMN_PLOT)));
        holder.thumbnail.setImageDrawable(null);

        String thumbnail = cursor.getString(cursor.getColumnIndex(COLUMN_THUMB));
        if (thumbnail.contains("content://")) {
            //holder.thumbnail.setImageURI(Uri.parse(movieItem.getThumbnail()));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(thumbnail));
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                holder.thumbnail.setImageBitmap(resized);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                holder.thumbnail.setBackground(Drawable.createFromStream(mContext.getAssets().open(thumbnail), null));
            } catch (IOException e) {
                Log.e("MovieInfo", "IOException: Failed to load the movie image from asset");
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}