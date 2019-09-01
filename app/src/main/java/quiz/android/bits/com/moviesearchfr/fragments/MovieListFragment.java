package quiz.android.bits.com.moviesearchfr.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import quiz.android.bits.com.moviesearchfr.R;
import quiz.android.bits.com.moviesearchfr.adapters.CustomCursorListAdapter;
import quiz.android.bits.com.moviesearchfr.listners.RecyclerItemClickListener;
import quiz.android.bits.com.moviesearchfr.models.MovieItem;
import quiz.android.bits.com.moviesearchfr.utils.Utils;

import static android.app.Activity.RESULT_OK;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_CAST;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_DIRECTOR;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_ID;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_PLOT;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_THUMB;
import static quiz.android.bits.com.moviesearchfr.database.MovieDatabaseHelper.COLUMN_TITLE;
import static quiz.android.bits.com.moviesearchfr.providers.MovieContentProvider.CONTENT_URI;
import static quiz.android.bits.com.moviesearchfr.providers.MovieContentProvider.SINGLE_CONTENT_URI;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MovieListFragment extends Fragment implements SwipeHelper.UnderlayButtonClickListener {

    private static final int REQUEST_PICK_PHOTO = 001;
    private static final int OPERATION_ADD = 1;
    private static final int OPERATION_EDIT = 2;
    private String TAG = MovieListFragment.class.getSimpleName();
    private OnFragmentInteractionListener mListener;
    private Utils utils;
    private int screenOrientation;

    private String[] movieNamesList;
    private String[] moviePlotsList;
    private String[] movieCastsList;
    private String[] movieDirectorsList;
    private RecyclerView mRecyclerView;
    private TextView titleView;
    private TextView plotView;

    public static String LIST_POSITION = "quiz.android.bits.com.movieassignment.list_position";
    public static String MOVIE_PLOT_URI = "quiz.android.bits.com.movieassignment.movie_plot";
    public static String MOVIE_TITLE_URI = "quiz.android.bits.com.movieassignment.movie_title";
    public static String MOVIE_IMAGE_URI = "quiz.android.bits.com.movieassignment.movie_poster_uri";
    public static String MOVIE_CAST_URI = "quiz.android.bits.com.movieassignment.movie_cast_uri";
    public static String MOVIE_DIRECTOR_URI = "quiz.android.bits.com.movieassignment.movie_director_uri";

    private HashMap<String, String> moviePlotHashMap = new HashMap<>();
    private HashMap<String, String> movieCastsHashMap = new HashMap<>();
    private HashMap<String, String> movieDirectorsHashMap = new HashMap<>();

    //private MovieDatabaseHelper movieDB;
    public Uri selectedImageUri;
    public EditText thumbnailURIView;
    ImageView imageView;
    private EditText movieDialogPlotView;
    private EditText movieDialogTitleView;
    private ImageView movieBackground;
    private AlertDialog alertDialog;
    private Context activityContext;
    private CustomCursorListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utils = new Utils();
        //movieDB = new MovieDatabaseHelper(getContext());

        movieNamesList = getResources().getStringArray(R.array.movie_list_data_feed);
        moviePlotsList = getResources().getStringArray(R.array.movie_plots_data_feed);
        movieCastsList = getResources().getStringArray(R.array.movie_cast_list_data_feed);
        movieDirectorsList = getResources().getStringArray(R.array.movie_director_list_data_feed);

        moviePlotHashMap = utils.getMoviePlotMap(moviePlotsList);
        movieCastsHashMap = utils.getMovieCastsMap(movieCastsList);
        movieDirectorsHashMap = utils.getMovieDirectorsMap(movieDirectorsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.movie_list_fragment, container, false);

        activityContext = view.getContext();

        mRecyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.addOnItemTouchListener(itemClickListener);

        mAdapter = new CustomCursorListAdapter(getContext(), null);
        mRecyclerView.setAdapter(mAdapter);

        setupItemTotTheRecyclerViewSwipeHelper();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(fabClickListener);

        screenOrientation = this.getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            titleView = view.findViewById(R.id.movie_info_title_textview);
            plotView = view.findViewById(R.id.movie_plot_textview);
            movieBackground = view.findViewById(R.id.movie_image);
        }

        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt(LAST_SELECTED_LIST_INDEX);
        }

        init();

        return view;
    }

    private void init() {
        getActivity().getSupportLoaderManager().initLoader(0, null, movieLoaderCallbacks);

        List<MovieItem> movieList = getAllMoviesThroughContentProvider();
        if (movieList.size() == 0) {
            movieList = getMovieItemlist();
            feedInitialDataToMovieTableUsingContentProvider(movieList);
        }

        // Updating the Landscape views with delay, because need to load the
        updateMovieDetailsForLandscapeViews(movieList.get(selectedIndex));
    }

    private void setupItemTotTheRecyclerViewSwipeHelper() {
        // adding item touch helper
        SwipeHelper swipeHelper = new SwipeHelper(getContext(), mRecyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        0,
                        Color.parseColor("#FF3C30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int selectedIndex) {
                                MovieListFragment.this.selectedIndex = selectedIndex;
                                showDeleteConfirmationDialog(getContext(), selectedIndex);
                            }
                        }
                ));

                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Edit",
                        0,
                        Color.parseColor("#FF9502"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int selectedIndex) {
                                MovieListFragment.this.selectedIndex = selectedIndex;
                                showAddOrEditItemDialog(getContext(), selectedIndex, OPERATION_EDIT);
                            }
                        }
                ));
            }
        };

        new ItemTouchHelper(swipeHelper).attachToRecyclerView(mRecyclerView);
    }


    private final String LAST_SELECTED_LIST_INDEX = "LAST_SELECTED_LIST_INDEX";
    private int selectedIndex;

    private View.OnClickListener fabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAddOrEditItemDialog(getContext(), -1, OPERATION_ADD);
        }
    };

    private RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(getContext(),
            mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(getSelectedMovieItemBundle(position));
                }
            } else {
                updateMovieDetailsForLandscapeViews(getSelectedMovieItemFromCP(position));
            }
            selectedIndex = position;
        }

        @Override
        public void onLongItemClick(View view, int position) {
        }
    });

    private void updateMovieDetailsForLandscapeViews(MovieItem item) {
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT)
            return;

        titleView.setText(item.getTitle());
        plotView.setText(item.getPlot());

        if (item.getThumbnail().contains("content://")) {
            movieBackground.setImageURI(Uri.parse(item.getThumbnail()));
        } else {
            try {
                movieBackground.setImageDrawable(Drawable.createFromStream(getActivity().getAssets().open(item.getThumbnail()), null));
            } catch (IOException e) {
                Log.e("MovieInfo", "IOException: Failed to load the movie image from asset");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(LAST_SELECTED_LIST_INDEX, selectedIndex);
    }


    @Override
    public void onResume() {
        getLoaderManager().restartLoader(0, null, movieLoaderCallbacks);
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(int pos) {
    }

    private ArrayList<MovieItem> getMovieItemlist() {
        ArrayList<MovieItem> movieList = new ArrayList<>();
        for (int i = 0; i < movieNamesList.length; i++) {
            String title = movieNamesList[i];
            String movie_id = utils.getMovieIdFromName(title);
            movieList.add(new MovieItem(utils.getId(title), title, movieCastsHashMap.get(movie_id),
                    movieDirectorsHashMap.get(movie_id), moviePlotHashMap.get(movie_id), movie_id + ".jpeg"));
        }
        return movieList;
    }

    private void feedInitialDataToMovieTableUsingContentProvider(List<MovieItem> movieList) {
        if (movieList == null) return;
        ContentValues[] allMovieContentValues = new ContentValues[movieList.size()];
        for (int i = 0; i < movieList.size(); i++) {
            allMovieContentValues[i] = getContentValues(movieList.get(i));
        }

        getContext().getContentResolver().bulkInsert(CONTENT_URI, allMovieContentValues);
    }

    private boolean updateMovieThroughContentProvider(MovieItem movie) {
        ContentValues values = getContentValues(movie);
        int countOfRows = getContext().getContentResolver().update(CONTENT_URI,
                values, COLUMN_ID + " = ?", new String[]{movie.getId() + ""});
        return countOfRows > 0;
    }

    private boolean deleteMovieThroughContentProvider(MovieItem movie) {
        int countOfRows = getContext().getContentResolver().delete(CONTENT_URI,
                COLUMN_ID + " = ?", new String[]{movie.getId() + ""});

        return countOfRows > 0;
    }

    private List<MovieItem> getAllMoviesThroughContentProvider() {
        String authority = CONTENT_URI.getAuthority();

        Cursor cursor = getContext().getContentResolver().query(CONTENT_URI, null, null, null, null);
        List<MovieItem> movieItems = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                movieItems.add(getMovieItemFromCursor(cursor));
            } while (cursor.moveToNext());

        } else {
            Log.d(" Adding Movie", "No Records Found");
        }

        return movieItems;
    }

    private MovieItem getMovieItemFromCursor(Cursor cursor) {
        MovieItem movieItem = new MovieItem();
        movieItem.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
        movieItem.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        movieItem.setCast(cursor.getString(cursor.getColumnIndex(COLUMN_CAST)));
        movieItem.setDirector(cursor.getString(cursor.getColumnIndex(COLUMN_DIRECTOR)));
        movieItem.setPlot(cursor.getString(cursor.getColumnIndex(COLUMN_PLOT)));
        movieItem.setThumbnail(cursor.getString(cursor.getColumnIndex(COLUMN_THUMB)));
        return movieItem;
    }

    private ContentValues getContentValues(MovieItem movie) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, movie.getId());
        values.put(COLUMN_TITLE, movie.getTitle());
        values.put(COLUMN_CAST, movie.getCast());
        values.put(COLUMN_DIRECTOR, movie.getDirector());
        values.put(COLUMN_PLOT, movie.getPlot());
        values.put(COLUMN_THUMB, movie.getThumbnail());
        return values;
    }

    private MovieItem getSelectedMovieItemFromCP(int position) {
        Uri queryUri = Uri.withAppendedPath(SINGLE_CONTENT_URI, mAdapter.getItemId(position) + "");
        Cursor cursor = getContext().getContentResolver().query(queryUri, null, null, null, null);
        MovieItem movieItem = null;
        if (cursor.moveToFirst()) {
            movieItem = getMovieItemFromCursor(cursor);
        }
        return movieItem;
    }

    private Bundle getSelectedMovieItemBundle(int position) {
        Uri queryUri = Uri.withAppendedPath(SINGLE_CONTENT_URI, mAdapter.getItemId(position) + "");
        Cursor cursor = getContext().getContentResolver().query(queryUri, null, null, null, null);
        Bundle bundle = new Bundle();

        if (cursor.moveToFirst()) {
            bundle.putInt(LIST_POSITION, position);
            bundle.putString(MOVIE_TITLE_URI, cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
            bundle.putString(MOVIE_CAST_URI, cursor.getString(cursor.getColumnIndex(COLUMN_CAST)));
            bundle.putString(MOVIE_PLOT_URI, cursor.getString(cursor.getColumnIndex(COLUMN_PLOT)));
            bundle.putString(MOVIE_DIRECTOR_URI, cursor.getString(cursor.getColumnIndex(COLUMN_DIRECTOR)));
            bundle.putString(MOVIE_IMAGE_URI, cursor.getString(cursor.getColumnIndex(COLUMN_THUMB)));
        }

        return bundle;
    }

    private void showAddOrEditItemDialog(Context context, final int selectedIndex, int oprType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(layout);
        movieDialogTitleView = layout.findViewById(R.id.title_edit_view);
        movieDialogPlotView = layout.findViewById(R.id.plot_edit_view);
        thumbnailURIView = layout.findViewById(R.id.thumbnail_uri);
        imageView = layout.findViewById(R.id.image_view);

        if (oprType == OPERATION_EDIT) {

            final MovieItem movieItem = getSelectedMovieItemFromCP(selectedIndex);

            movieDialogTitleView.setText(movieItem.getTitle());
            movieDialogPlotView.setText(movieItem.getPlot());
            thumbnailURIView.setText(movieItem.getThumbnail());

            layout.findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MovieItem updatedItem = new MovieItem(movieItem.getId(), movieDialogTitleView.getText().toString(),
                            movieItem.getCast(), movieItem.getDirector(), movieDialogPlotView.getText().toString(),
                            thumbnailURIView.getText().toString());

                    boolean isUpdated = updateMovieThroughContentProvider(updatedItem);
                    if (isUpdated) {
                        getActivity().getSupportLoaderManager().restartLoader(0, null, movieLoaderCallbacks);
                    }
                    alertDialog.dismiss();
                }
            });
        }

        if (oprType == OPERATION_ADD) {
            layout.findViewById(R.id.dialog_ok).setOnClickListener(addMovieDialogListener);
        }
        layout.findViewById(R.id.dialog_cancel).setOnClickListener(addMovieDialogListener);
        layout.findViewById(R.id.open_gallery).setOnClickListener(addMovieDialogListener);

        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog(Context context, final int selectedIndex) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Delete entry");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) { //OK
                    boolean isDeleted = deleteMovieThroughContentProvider(getSelectedMovieItemFromCP(selectedIndex));
                    if (isDeleted) {
                        getActivity().getSupportLoaderManager().restartLoader(0, null, movieLoaderCallbacks);
                    }
                }
            }
        });
        alert.setNegativeButton(android.R.string.no, null);
        alert.show();
    }

    View.OnClickListener addMovieDialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.open_gallery) {
                openImageChooser();

            } else if (v.getId() == R.id.dialog_ok) {

                String title = movieDialogTitleView.getText().toString();
                MovieItem movieItem = new MovieItem(utils.getId(title), title, "Default cast", "Default director", movieDialogPlotView.getText().toString(),
                        thumbnailURIView.getText().toString());

                Uri uri = getContext().getContentResolver().insert(CONTENT_URI, getContentValues(movieItem));
                getActivity().getSupportLoaderManager().restartLoader(0, null, movieLoaderCallbacks);

                alertDialog.dismiss();

            } else if (v.getId() == R.id.dialog_cancel) {
                if (alertDialog != null && alertDialog.isShowing())
                    alertDialog.dismiss();
            }
        }
    };


    /*loader*/

    LoaderCallbacks<Cursor> movieLoaderCallbacks = new LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case 0:
                    return new CursorLoader(getContext(), CONTENT_URI, null, null, null, null);
                default:
                    throw new IllegalArgumentException("no id handled!");
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d("", "Cursor : " + cursor.getCount());
            mAdapter.swapCursor(cursor);
            mRecyclerView.requestLayout();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    };

    /*loader end*/


    public void openImageChooser() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_PHOTO);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_PHOTO) {
                selectedImageUri = intent.getData();
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageURI(selectedImageUri);
                thumbnailURIView.setText(selectedImageUri + "");
            }
        }
    }

    // to handle fragment click callback
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Bundle movieBundle);
    }

}
