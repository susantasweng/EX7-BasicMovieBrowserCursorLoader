package quiz.android.bits.com.moviesearchfr.models;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieItem implements Parcelable {
    private int id;
    private String title;
    private String cast;
    private String director;
    private String plot;
    private String thumbnail;

    public MovieItem() {
    }

    public MovieItem(int id, String title, String plot, String thumbnail) {
        this.id = id;
        this.title = title;
        this.plot = plot;
        this.thumbnail = thumbnail;
    }

    public MovieItem(int id, String title, String cast, String director, String plot) {
        this.id = id;
        this.title = title;
        this.cast = cast;
        this.director = director;
        this.plot = plot;
    }

    public MovieItem(int id, String title, String cast, String producer, String plot, String thumbnail) {
        this.id = id;
        this.title = title;
        this.cast = cast;
        this.director = producer;
        this.plot = plot;
        this.thumbnail = thumbnail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected MovieItem(Parcel in) {
        id = in.readInt();
        title = in.readString();
        cast = in.readString();
        director = in.readString();
        plot = in.readString();
        thumbnail = in.readString();
    }

    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(cast);
        dest.writeString(director);
        dest.writeString(plot);
        dest.writeString(thumbnail);
    }
}
