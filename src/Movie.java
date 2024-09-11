import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Movie {
    private String id;

    private String title;

    private String year;

    private String director;

    private List<String> categories;

    public Movie() {
        this.id = "";
        this.title = "";
        this.year = "";
        this.director = "";
        this.categories = new ArrayList<>();
    }

    public void setTitle(String title){
        this.title = title;
    }
    public void setYear(String year){
        this.year = year;
    }
    public void setId(String id){
        this.id = id;
    }
    public void setDirector(String director){
        this.director = director;
    }
    public void setCategories(String category){
        this.categories.add(category);
    }
    public String getTitle(){
        return this.title;
    }
    public String getYear(){
        return this.year;
    }
    public String getId(){
        return this.id;
    }
    public String getDirector(){
        return this.director;
    }
    public List<String> getCategories(){
        return this.categories;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("Title:" + getTitle());
        sb.append(", ");
        sb.append("ID:" + getId());
        sb.append(", ");
        sb.append("Year:" + getYear());
        sb.append(", ");
        sb.append("Director:" + getDirector());
        sb.append(", ");
        sb.append("Categories:" + getCategories().toString());
        sb.append(".");

        return sb.toString();
    }
}
