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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// Declaring a WebServlet called MovieListServlet, which maps to url "/api/MovieList"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/MovieList")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/slave/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Default value for entriesPerPage
        int entriesPerPage = 20;
        int currentPage = request.getParameter("page") == null ? 1 : Integer.parseInt(request.getParameter("page"));

        int offset = (currentPage - 1) * entriesPerPage;
        // Try to get the entriesPerPage from the session
        Integer sessionEntriesPerPage = (Integer) request.getSession().getAttribute("entriesPerPage");

        // If the attribute is found and is not null, use it
        if (sessionEntriesPerPage != null) {
            entriesPerPage = sessionEntriesPerPage;
        }

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (out; Connection conn = dataSource.getConnection()) {

            // Declare our statement
            PreparedStatement statement = null;

            // Whitelisted sort fields and directions
            List<String> validSortFields = Arrays.asList("r.rating", "m.title");
            List<String> validSortOrders = Arrays.asList("asc", "desc");

            // Get parameters and default to safe values if necessary
            String sortFieldParam = request.getParameter("sortby");
            String sortOrderParam = request.getParameter("sortorder");

            // Validate sort type and direction
            String sortType = validSortFields.contains(sortFieldParam) ? sortFieldParam : "r.rating";
            String sortOrder = validSortOrders.contains(sortOrderParam) ? sortOrderParam : "desc";

            // Declare and Default Query to Top 20 movies, reassign if given parameters
            String query = "SELECT m.title, m.year, m.director, r.rating, m.id, m.price " +
                    "FROM movies as m " +
                    "INNER JOIN ratings AS r ON r.movieId = m.id " +
                    "ORDER BY " + sortType + " " + sortOrder + " " +
                    "LIMIT ? OFFSET ?";

            // Case 1: Check if user is browsing Genres
            String browseGenreId = request.getParameter("browseGenreId");
            String browseTitle = request.getParameter("browseTitle");

            String searchmovieTitle = request.getParameter("movieTitle");
            String searchyearReleased = request.getParameter("yearReleased");
            String searchdirector = request.getParameter("director");
            String searchmovieStar = request.getParameter("movieStar");


            if (browseGenreId != null) {
                System.out.println("Browsing for genre: " + browseGenreId);
                query = "SELECT m.title, m.year, m.director, r.rating, m.id, m.price " +
                        "FROM movies as m, genres_in_movies as gim, genres as g, ratings as r " +
                        "WHERE m.id = gim.movieId AND gim.genreId = g.id AND r.movieId = m.id AND g.id = ? " +
                        "ORDER BY " + sortType + " " + sortOrder + " " +
                        "LIMIT ? OFFSET ?";

                // Prepare Query
                statement = conn.prepareStatement(query);

                // Set selected genre as "?" variable
                statement.setInt(1, Integer.parseInt(browseGenreId));
                // Assuming entriesPerPage and offset are already validated as integers
                statement.setInt(2, entriesPerPage);
                statement.setInt(3, offset);
            }
            // Case 2: Check if user browsing titles
            else if (browseTitle != null){
                System.out.println("Browsing for title: " + browseTitle);

                if (browseTitle.equals("*")) {
                    query = "SELECT m.title, m.year, m.director, r.rating, m.id, m.price " +
                            "FROM movies as m, ratings as r " +
                            "WHERE r.movieId = m.id AND m.title NOT REGEXP '^[0-9A-Za-z]' " +
                            "ORDER BY " + sortType + " " + sortOrder + " " +
                            "LIMIT ? OFFSET ? ";
                    statement = conn.prepareStatement(query);
                    statement.setInt(1, entriesPerPage);
                    statement.setInt(2, offset);
                }
                else {
                    query = "SELECT m.title, m.year, m.director, r.rating, m.id, m.price " +
                            "FROM movies as m, ratings as r " +
                            "WHERE r.movieId = m.id AND m.title LIKE ? " +
                            "ORDER BY " + sortType + " " + sortOrder + " " +
                            "LIMIT ? OFFSET ?";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, browseTitle + "%");
                    statement.setInt(2, entriesPerPage);
                    statement.setInt(3, offset);
                }
            }
            // Case 3: User is using search functionality
            else if (searchmovieTitle != null || searchyearReleased != null || searchdirector != null || searchmovieStar != null){
                int numParams = 0;

                query = "SELECT m.title, m.year, m.director, r.rating, m.id, m.price, "
                        + "GROUP_CONCAT(s.name ORDER BY s.name ASC SEPARATOR ', ') AS star_names "
                        + "FROM movies m "
                        + "JOIN ratings r ON r.movieId = m.id "
                        + "JOIN stars_in_movies sm ON sm.movieId = m.id "
                        + "JOIN stars s ON s.id = sm.starId ";

                String whereClause = "";

                // NOTE: Project 4, modified to support full-text searching (WITH FULL WORDS)
                if (searchmovieTitle != null && !searchmovieTitle.isEmpty()){
                    whereClause += "MATCH(m.title) AGAINST(? IN BOOLEAN MODE) ";
                    numParams += 1;
                }
                if (searchyearReleased != null && !searchyearReleased.isEmpty()){
                    whereClause += (whereClause.isEmpty() ? "" : "AND ") + "m.year = ? ";
                    numParams += 1;
                }
                if (searchdirector != null && !searchdirector.isEmpty()){
                    whereClause += (whereClause.isEmpty() ? "" : "AND ") + "m.director LIKE ? ";
                    numParams += 1;
                }

                if (!whereClause.isEmpty()) {
                    query += "WHERE " + whereClause;
                }

                query += "GROUP BY m.id, m.title, m.year, m.director, r.rating, m.price ";  // Add GROUP BY clause here [Needed for "HAVING"]

                if (searchmovieStar != null && !searchmovieStar.isEmpty()){
                    query += "HAVING star_names LIKE ? ";
                    numParams += 1;
                }

                query += "ORDER BY " + sortType + " " + sortOrder + " " +
                        "LIMIT ? OFFSET ?";

                // Prepare the statement with the query
                statement = conn.prepareStatement(query);

                // Set parameters for the prepared statement
                int paramIndex = 1; // Start from 1 as SQL parameters are 1-based
                if (searchmovieTitle != null && !searchmovieTitle.isEmpty()) {
                    // Split the searchmovieTitle into individual keywords
                    String[] keywords = searchmovieTitle.split("\\s+");

                    // Append an asterisk to each keyword and join them back with a space
                    StringBuilder modifiedTitle = new StringBuilder();
                    for (String keyword : keywords) {
                        modifiedTitle.append(keyword).append("* ").append(" ");
                    }

                    // Trim the trailing space
                    String finalSearchTitle = modifiedTitle.toString().trim();

                    // Searches via full-text prefix mode
                    statement.setString(paramIndex++, finalSearchTitle);
                }
                if (searchyearReleased != null && !searchyearReleased.isEmpty()) {
                    statement.setInt(paramIndex++, Integer.parseInt(searchyearReleased));
                }
                if (searchdirector != null && !searchdirector.isEmpty()) {
                    statement.setString(paramIndex++, searchdirector + "%");
                }
                if (searchmovieStar != null && !searchmovieStar.isEmpty()) {
                    statement.setString(paramIndex++, "%" + searchmovieStar + "%");
                }

                // Now set the entriesPerPage and offset
                statement.setInt(paramIndex++, entriesPerPage);
                statement.setInt(paramIndex++, offset);
            }
            else {
                // Prepare Default Query
                statement = conn.prepareStatement(query);
                statement.setInt(1, entriesPerPage);
                statement.setInt(2, offset);
            }

            // Perform the query
            System.out.println("queue:" + statement.toString());
            ResultSet resultSet = statement.executeQuery();

            // Prepare JsonArray to store movie data
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (resultSet.next()) {
                // Get and set locally all movie data attributes.
                String movieID = resultSet.getString("id");
                String moviePrice = resultSet.getString("price");
                String movieTitle = resultSet.getString("title");
                String year = resultSet.getString("year");
                String director = resultSet.getString("director");
                String rating = resultSet.getString("rating");
                JsonArray genres = new JsonArray();
                JsonArray genresIds = new JsonArray();
                JsonArray stars = new JsonArray();
                JsonArray starsIds = new JsonArray();

                // TODO: Make a method gathering data given a SQL query
                // prepare genres query
                String genreQuery = "SELECT g.name, g.id " +
                        "FROM genres_in_movies AS gm " +
                        "INNER JOIN genres AS g ON g.id = gm.genreId " +
                        "WHERE gm.movieId = \"" + movieID + "\" " +
                        "ORDER BY g.name " +
                        "LIMIT 3";

                // Create a Statement and a corresponding ResultSet to iterate through
                Statement genreStatement = conn.createStatement();
                ResultSet genresResultSet = genreStatement.executeQuery(genreQuery);

                // Record each genre of the movie (up to 3)
                while (genresResultSet.next()) {
                    genres.add(genresResultSet.getString("name"));
                    genresIds.add(genresResultSet.getInt("id"));
                }

                // prepare stars query
                String starQuery =  "SELECT s.id, s.name, COUNT(sim2.movieId) AS num_movies " +
                        "FROM stars AS s " +
                        "JOIN stars_in_movies AS sim1 ON s.id = sim1.starId " +
                        "JOIN stars_in_movies AS sim2 ON sim1.starId = sim2.starId " +
                        "WHERE sim1.movieId = \"" + movieID + "\"" +
                        "GROUP BY s.id, s.name " +
                        "ORDER BY num_movies DESC, s.name " +
                        "LIMIT 3";

                // Create a Statement and a corresponding ResultSet to iterate through
                Statement starsStatement = conn.createStatement();
                ResultSet starsResultSet = starsStatement.executeQuery(starQuery);

                // Record each star in the movie (up to 3)
                while (starsResultSet.next()) {
                    stars.add(starsResultSet.getString("name"));
                    starsIds.add(starsResultSet.getString("id"));
                }

                // Store the data within a JsonObject, then storing the data within a JsonArray
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("movieId", movieID);
                jsonObject.addProperty("title", movieTitle);
                jsonObject.addProperty("year", year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("rating", rating);
                jsonObject.add("genres", genres);
                jsonObject.add("genresIds", genresIds);
                jsonObject.add("stars", stars);
                jsonObject.add("starsIds", starsIds);
                jsonObject.addProperty("price", moviePrice);
                jsonArray.add(jsonObject);
                // Close the genre and star  statements/result sets
                genresResultSet.close();
                starsResultSet.close();

                genreStatement.close();
                starsStatement.close();
            }
            resultSet.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}


