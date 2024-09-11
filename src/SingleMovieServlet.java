import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slave/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (out; Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
//            String query = "SELECT m.title, m.year, m.director, r.rating, m.id " +
//                           "FROM movies as m INNER JOIN ratings AS r ON r.movieId = m.id" +
//                           "WHERE m.id = " + id;

            String query = "SELECT * from movies as m, ratings as r where r.movieId = m.id and r.movieId = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating");
                JsonArray genres = new JsonArray();
                JsonArray genresIds = new JsonArray();
                JsonArray movieStars = new JsonArray();
                JsonArray starsIds = new JsonArray();

                // Corrected genreQuery to prevent SQL injection
                String genreQuery = "SELECT g.name, g.id " +
                        "FROM genres_in_movies AS gm " +
                        "INNER JOIN genres AS g ON g.id = gm.genreId " +
                        "WHERE gm.movieId = ? " + // Use a placeholder instead of concatenating the id
                        "ORDER BY g.name";

                // Create a PreparedStatement and set the id parameter
                PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
                genreStatement.setString(1, id); // Set the id parameter
                ResultSet genresResultSet = genreStatement.executeQuery();

                // Record each genre of the movie (up to 3)
                while (genresResultSet.next()) {
                    genres.add(genresResultSet.getString("name"));
                    genresIds.add(genresResultSet.getInt("id"));
                }

                genresResultSet.close();
                genreStatement.close();

                // Corrected starQuery to prevent SQL injection
                String starQuery =  "SELECT s.id, s.name, COUNT(sim2.movieId) AS num_movies " +
                        "FROM stars AS s " +
                        "JOIN stars_in_movies AS sim1 ON s.id = sim1.starId " +
                        "JOIN stars_in_movies AS sim2 ON sim1.starId = sim2.starId " +
                        "WHERE sim1.movieId = ? " + // Use a placeholder instead of concatenating the id
                        "GROUP BY s.id, s.name " +
                        "ORDER BY num_movies DESC, s.name";

                // Create a PreparedStatement and set the id parameter
                PreparedStatement starsStatement = conn.prepareStatement(starQuery);
                starsStatement.setString(1, id); // Set the id parameter
                ResultSet starsResultSet = starsStatement.executeQuery();

                // Record each star in the movie (up to 3)
                while (starsResultSet.next()) {
                    movieStars.add(starsResultSet.getString("name"));
                    starsIds.add(starsResultSet.getString("id"));
                }

                starsResultSet.close();
                starsStatement.close();

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonObject.add("genres", genres);
                jsonObject.add("genresIds", genresIds);
                jsonObject.add("movieStars", movieStars);
                jsonObject.add("starsIds", starsIds);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
