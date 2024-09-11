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


@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add_movie")
public class AddMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Log that the doPost method has been entered
        System.out.println("doPost called in AddMovieServlet");

        try (out; Connection conn = dataSource.getConnection()) {
            // Log that a connection has been established
            System.out.println("Database connection established");

            // Retrieve form parameters
            String movieTitle = request.getParameter("movieTitle");
            String movieYearStr = request.getParameter("movieYear");
            String movieDirector = request.getParameter("movieDirector");
            String starName = request.getParameter("starName");
            String birthYearStr = request.getParameter("birthYear");
            String genre = request.getParameter("genre");

            // Log retrieved form parameters
            System.out.println("Retrieved movieTitle: " + movieTitle);
            System.out.println("Retrieved movieYear: " + movieYearStr);
            System.out.println("Retrieved movieDirector: " + movieDirector);
            System.out.println("Retrieved starName: " + starName);
            System.out.println("Retrieved birthYearStr: " + birthYearStr);
            System.out.println("Retrieved genre: " + genre);

            Integer movieYear = null;
            Integer birthYear = null;

            // Validate and convert movieYear to Integer
            try {
                movieYear = Integer.parseInt(movieYearStr);
            } catch (NumberFormatException e) {
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Movie year must be a valid integer.");
                out.write(responseJsonObject.toString());
                return;
            }

            // Validate and convert birthYear to Integer if it's not empty
            if (birthYearStr != null && !birthYearStr.trim().isEmpty()) {
                try {
                    birthYear = Integer.parseInt(birthYearStr);
                } catch (NumberFormatException e) {
                    JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Birth year must be a valid integer.");
                    out.write(responseJsonObject.toString());
                    return;
                }
            }

            // Log the parsed integer values
            System.out.println("Parsed movieYear: " + movieYear);
            System.out.println("Parsed birthYear: " + birthYear);

            // Prepare SQL statement to call the stored procedure
            String query = "CALL add_movie(?, ?, ?, ?, ?, ?);";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, movieTitle);
                statement.setInt(2, movieYear);
                statement.setString(3, movieDirector);
                statement.setString(4, starName);
                if (birthYear != null) {
                    statement.setInt(5, birthYear);
                } else {
                    statement.setNull(5, java.sql.Types.INTEGER);
                }
                statement.setString(6, genre);

                // Log the prepared statement
                System.out.println("Prepared SQL statement: " + statement);

                // Execute SQL statement and check for a result set
                boolean hasResultSet = statement.execute();

                // Prepare response JSON
                JsonObject responseJsonObject = new JsonObject();

                if (hasResultSet) {
                    try (ResultSet rs = statement.getResultSet()) {
                        if (rs.next()) {
                            // If a result set is present, it means an error occurred
                            String error = rs.getString("error");
                            System.out.println("Stored procedure error: " + error);

                            responseJsonObject.addProperty("status", "fail");
                            responseJsonObject.addProperty("message", "Error adding movie: " + error);
                        }
                    }
                } else {
                    // If no result set, the update was successful
                    int rowsAffected = statement.getUpdateCount();
                    System.out.println("SQL execution completed, rows affected: " + rowsAffected);


                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Movie added successfully.");

                }

                out.write(responseJsonObject.toString());
            }
        } catch (Exception e) {
            // Log the exception
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();

            // Write error message JSON
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Error adding movie: " + e.getMessage());
            out.write(responseJsonObject.toString());
        } finally {
            out.close();
        }
    }
}