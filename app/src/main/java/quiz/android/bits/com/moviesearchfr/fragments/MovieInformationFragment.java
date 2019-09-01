package quiz.android.bits.com.moviesearchfr.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import quiz.android.bits.com.moviesearchfr.R;
import quiz.android.bits.com.moviesearchfr.utils.Utils;

public class MovieInformationFragment extends Fragment {

    private ImageView movieBackground;
    private TextView titleView;
    private TextView plotView;
    private TextView castView;
    private TextView directorView;
    private int screenOrientation;

    private String movieName;
    private String movieCast;
    private String movieDirector;
    private String moviePlot;
    private String thumbnail;
    private Utils utils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utils = new Utils();
        if (getArguments() != null) {
            movieName = getArguments().getString(MovieListFragment.MOVIE_TITLE_URI);
            movieCast = getArguments().getString(MovieListFragment.MOVIE_CAST_URI);
            movieDirector = getArguments().getString(MovieListFragment.MOVIE_DIRECTOR_URI);
            moviePlot = getArguments().getString(MovieListFragment.MOVIE_PLOT_URI);
            thumbnail = getArguments().getString(MovieListFragment.MOVIE_IMAGE_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_info_fragment, container, false);
        screenOrientation = getResources().getConfiguration().orientation;

        titleView = view.findViewById(R.id.movie_info_title_textview);
        plotView = view.findViewById(R.id.movie_plot_textview);
        castView = view.findViewById(R.id.movie_cast_textview);
        directorView = view.findViewById(R.id.movie_director_textview);
        movieBackground = view.findViewById(R.id.movie_image);

        updateGeneralViews();

        return view;
    }

    private void updateGeneralViews() {
        titleView.setText(movieName);
        plotView.setText(moviePlot);
        castView.setText(movieCast);
        directorView.setText(movieDirector);

        if (thumbnail.contains("content://")) {
            movieBackground.setImageURI(Uri.parse(thumbnail));

        } else {
            try {
                movieBackground.setBackground(Drawable.createFromStream(getActivity().getAssets().open(thumbnail), null));
            } catch (IOException e) {
                Log.e("MovieInfo", "IOException: Failed to load the movie image from asset");
                e.printStackTrace();
            }
        }
    }
}
