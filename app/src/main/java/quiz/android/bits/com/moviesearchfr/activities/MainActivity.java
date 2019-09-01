package quiz.android.bits.com.moviesearchfr.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import quiz.android.bits.com.moviesearchfr.R;
import quiz.android.bits.com.moviesearchfr.fragments.MovieInformationFragment;
import quiz.android.bits.com.moviesearchfr.fragments.MovieListFragment;

public class MainActivity extends AppCompatActivity implements MovieListFragment.OnFragmentInteractionListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private FragmentManager fragmentManager;
    MovieListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        if(savedInstanceState == null) {
            displayMovieListFragment();
        }
    }

    private void displayMovieListFragment() {
        listFragment = new MovieListFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, listFragment, MovieListFragment.class.getName());
        fragmentTransaction.addToBackStack(MovieListFragment.class.getName());
        fragmentTransaction.commit();
    }

    private void displayMovieInfoFragment(Bundle movieBundle) {
        MovieInformationFragment infoFragment = new MovieInformationFragment();
        infoFragment.setArguments(movieBundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(getCurrentFragment());
        fragmentTransaction.add(R.id.fragment_container, infoFragment, MovieInformationFragment.class.getName());
        fragmentTransaction.addToBackStack(MovieInformationFragment.class.getName());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public Fragment getCurrentFragment() {
        return fragmentManager.findFragmentById(R.id.fragment_container);
    }

    @Override
    public void onFragmentInteraction(Bundle movieBundle) {
        displayMovieInfoFragment(movieBundle);
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFragment() instanceof MovieListFragment) {
            this.finish();
        }
        super.onBackPressed();
    }
}
