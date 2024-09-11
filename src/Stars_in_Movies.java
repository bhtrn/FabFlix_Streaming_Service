import java.util.ArrayList;
import java.util.List;

public class Stars_in_Movies {
    private String movieId;
    private String movieName;
    private String starName;

    public Stars_in_Movies() {
        this.movieId = "";
        this.starName = "";
    }

    public void setStarname(String star){
        this.starName = star;
    }
    public void setMovieId(String movie){
        this.movieId = movie;
    }
    public String getMovieId(){
        return this.movieId;
    }
    public String getStarname(){
        return this.starName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star ID:" + getStarname());
        sb.append(" - ");
        sb.append("Movie ID:" + getMovieId());

        return sb.toString();
    }
}
