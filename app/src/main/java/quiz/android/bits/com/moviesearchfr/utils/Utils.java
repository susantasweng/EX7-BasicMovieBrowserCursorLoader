package quiz.android.bits.com.moviesearchfr.utils;

import java.util.HashMap;

public class Utils {

    public String getMovieIdFromName(String movieName) {
        movieName = movieName.replaceAll("[$&+,:;=?@#!<>.^*()%]", "");
        movieName = (movieName.replaceAll(" ", "_"));
        return movieName.toLowerCase();
    }

    public HashMap<String, String> getMoviePlotMap(String[] moviePlotsList) {
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        for (String moviePlot:moviePlotsList) {
            String[] fields = moviePlot.split("\\|");
            hashMap.put(fields[0], fields[1]);
        }
        return hashMap;
    }

    public HashMap<String, String> getMovieCastsMap(String[] movieCastsList) {
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        for (String moviePlot:movieCastsList) {
            String[] fields = moviePlot.split("\\|");
            hashMap.put(fields[0], fields[1]);
        }
        return hashMap;
    }

    public HashMap<String, String> getMovieDirectorsMap(String[] movieDirectorsList) {
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        for (String moviePlot:movieDirectorsList) {
            String[] fields = moviePlot.split("\\|");
            hashMap.put(fields[0], fields[1]);
        }
        return hashMap;
    }

    public int getId(String text) {
        return Math.abs(text.hashCode());
    }
}
