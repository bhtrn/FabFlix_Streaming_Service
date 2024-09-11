import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import java.io.IOException;
import java.sql.*;

import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataInsertion {

    private List<Movie> newMovies;
    private List<Star> newStars;
    private List<Stars_in_Movies> newSims;

    private String url = "jdbc:mysql://localhost:3306/moviedb";
    private String user = "root";
    private String password = "hunman123 4";

    Map<String, String> codeCategoryMap;
    private Connection conn = null;

    public void extract(){

        MovieParser mp = new MovieParser();
        mp.runExample();
        newMovies = mp.getParsedFilms();

        StarParser sp = new StarParser();
        sp.runExample();
        newStars = sp.getStars();

        Stars_in_MoviesParser spm = new Stars_in_MoviesParser();
        spm.runExample();
        newSims = spm.getSims();

        codeCategoryMap = new HashMap<>();

        // Add key-value pairs to the dictionary
        codeCategoryMap.put("Susp", "thriller");
        codeCategoryMap.put("CnR", "cops and robbers");
        codeCategoryMap.put("Dram", "drama");
        codeCategoryMap.put("West", "western");
        codeCategoryMap.put("Myst", "mystery");
        codeCategoryMap.put("S.F.", "science fiction");
        codeCategoryMap.put("Advt", "adventure");
        codeCategoryMap.put("Horr", "horror");
        codeCategoryMap.put("Romt", "romantic");
        codeCategoryMap.put("Comd", "comedy");
        codeCategoryMap.put("Musc", "musical");
        codeCategoryMap.put("Docu", "documentary");
        codeCategoryMap.put("Porn", "pornography, including soft");
        codeCategoryMap.put("Noir", "black");
        codeCategoryMap.put("BioP", "biographical Picture");
        codeCategoryMap.put("TV", "TV show");
        codeCategoryMap.put("TVs", "TV series");
        codeCategoryMap.put("TVm", "TV miniseries");
    }

    public void insertMovies() throws IOException, NamingException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        try {
            conn = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = null;
            String searchQuery = "SELECT * FROM movies WHERE id = ?";
            String insertQuery = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";

            for (Movie movie : newMovies) {
                // Check if the movie has values for id, title, year, and director
                if (movie.getId() == null || movie.getTitle() == null || movie.getYear() == null || movie.getDirector() == null) {
                    System.out.println("Movie missing required fields: " + movie);
                }

                PreparedStatement selectStatement = conn.prepareStatement(searchQuery);
                PreparedStatement insertStatement = conn.prepareStatement(insertQuery);
                // Check if the movie already exists in the database
                selectStatement.setString(1, movie.getId());
                ResultSet resultSet = selectStatement.executeQuery();

                if (resultSet.next()) {
                    System.out.println("Movie with ID " + movie.getId() + " already exists in the database.");
                    continue; // Skip this movie if it already exists
                }

                // Insert the movie into the database
                insertStatement.setString(1, movie.getId());
                insertStatement.setString(2, movie.getTitle());
                insertStatement.setString(3, movie.getYear());
                insertStatement.setString(4, movie.getDirector());
                insertStatement.executeUpdate();

                String searchQuery2 = "SELECT * FROM genres WHERE name = ?";
                String insertQuery2 = "INSERT INTO genres (id, name) VALUES (?, ?)";

                if (movie.getCategories().size() > 0){
                    for (String cat : movie.getCategories()){
                        String genre = codeCategoryMap.get(cat);
                        if (genre == null){
                            continue;
                        }
                        selectStatement = conn.prepareStatement(searchQuery2);
                        selectStatement.setString(1, genre);
                        resultSet = selectStatement.executeQuery();

                        if (!(resultSet.next())) {
                            String searchQuery3 = "SELECT MAX(id) AS max_id FROM genres";
                            selectStatement = conn.prepareStatement(searchQuery3);
                            resultSet = selectStatement.executeQuery();
                            resultSet.next();
                            int newId = resultSet.getInt("max_id");

                            insertStatement = conn.prepareStatement(insertQuery2);
                            insertStatement.setInt(1, newId + 1);
                            insertStatement.setString(2, genre);
                            insertStatement.executeUpdate();
                        }
                        String searchQuery4 = "SELECT * FROM genres WHERE name = ?";
                        selectStatement = conn.prepareStatement(searchQuery4);
                        selectStatement.setString(1, genre);
                        resultSet = selectStatement.executeQuery();

                        resultSet.next();
                        int id = resultSet.getInt("id");
                        String insertQuery3 = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
                        insertStatement = conn.prepareStatement(insertQuery3);
                        insertStatement.setInt(1, id);
                        insertStatement.setString(2, movie.getId());
                        insertStatement.executeUpdate();

                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void insertStars(){
        try {
            conn = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = null;
            String searchQuery = "SELECT * FROM stars WHERE name = ? AND birthYear = ?";
            String searchQuery2 = "Select * FROM stars WHERE name = ?";
            String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            String insertQuery2 = "INSERT INTO stars (id, name) VALUES (?, ?)";
            for (Star star : newStars) {
                // Check if the movie has values for id, title, year, and director
                if (star.getName() == null || star.getBirthYear() == null) {
                    System.out.println("Star missing required fields: " + star);
                }
                PreparedStatement insertStatement;
                PreparedStatement selectStatement;
                if (star.getBirthYear() != null && !(star.getBirthYear().isEmpty())) {
                    selectStatement = conn.prepareStatement(searchQuery);
                    selectStatement.setString(1, star.getName());
                    selectStatement.setInt(2, Integer.parseInt(star.getBirthYear()));
                }
                else{
                    selectStatement = conn.prepareStatement(searchQuery2);
                    selectStatement.setString(1, star.getName());
                }

                ResultSet resultSet = selectStatement.executeQuery();
                if (!(resultSet.next())) {
                    String currentId = "SELECT MAX(id) as max_id FROM stars";
                    PreparedStatement idStatement = conn.prepareStatement(currentId);
                    resultSet = idStatement.executeQuery();
                    resultSet.next();

                    String id = resultSet.getString("max_id");

                    String prefix = id.replaceAll("[0-9]*$", ""); // Extracts letters from the end of the string

                    // Extract the numeric part from the maximum ID
                    int maxNumber = Integer.parseInt(id.substring(prefix.length()));

                    // Increment the maximum number by 1 to get the next number
                    int nextNumber = maxNumber + 1;

                    // Construct the new ID
                    String newId = prefix + nextNumber;

                    if (star.getBirthYear() != null && !(star.getBirthYear().isEmpty())) {
                        insertStatement = conn.prepareStatement(insertQuery);
                        insertStatement.setString(1, newId);
                        insertStatement.setString(2, star.getName());
                        insertStatement.setInt(3, Integer.parseInt(star.getBirthYear()));
                    }
                    else{
                        insertStatement = conn.prepareStatement(insertQuery2);
                        insertStatement.setString(1, newId);
                        insertStatement.setString(2, star.getName());
                    }
                    insertStatement.executeUpdate();
                }
                else{
                    System.out.println("Star with name " + star.getName() + " already exists in the database.");
                }
            }

        }catch (Exception e){
            e.printStackTrace();

        }
    }

    public void insertSims(){
        try {
            conn = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = null;
            String searchQuery = "SELECT s.id AS star_id " +
                    "FROM stars s " +
                    "INNER JOIN stars_in_movies sim ON s.id = sim.starId " +
                    "INNER JOIN movies m ON sim.movieId = m.id " +
                    "WHERE s.name = ? AND m.id = ?";

            String insertQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
            for (Stars_in_Movies sim : newSims) {
                if (sim.getMovieId() != null && sim.getStarname() != null) {
                    statement = conn.prepareStatement(searchQuery);
                    statement.setString(1, sim.getStarname());
                    statement.setString(2, sim.getMovieId());
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        String starId = resultSet.getString("star_id");
                        statement = conn.prepareStatement(insertQuery);
                        statement.setString(1, starId);
                        statement.setString(2, sim.getMovieId());
                        statement.executeUpdate();

                    }
                    else{

                    }
                }
                else{
                    System.out.println("Invalid Entry: " + sim);
                }
            }

        }catch (Exception e){
            e.printStackTrace();

        }
    }

    public static void main(String[] args) throws NamingException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        DataInsertion dataInsertion = new DataInsertion();
        dataInsertion.extract();
        dataInsertion.insertMovies();
        dataInsertion.insertStars();
//        dataInsertion.insertSims();
    }
}
