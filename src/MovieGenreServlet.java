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
import java.sql.ResultSet;
import java.sql.Statement;

// Gather all the unique genres
@WebServlet(name = "MovieGenreServlet", urlPatterns = "/api/genres")
public class MovieGenreServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        // We use this to return the data
        PrintWriter out = response.getWriter();

        try (out; Connection conn = dataSource.getConnection()) {
            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT name, id FROM genres ORDER BY name ASC";

            // Execute the query
            ResultSet resultSet = statement.executeQuery(query);

            JsonArray genresJson = new JsonArray();

            // Iterate each genre, adding it to the JsonArray
            while (resultSet.next()) {
                JsonObject genreJsonObject = new JsonObject();

                genreJsonObject.addProperty("id", resultSet.getInt("id"));
                genreJsonObject.addProperty("name", resultSet.getString("name"));

                genresJson.add(genreJsonObject);
            }

            resultSet.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + genresJson.size() + " genres");

            // Write JSON string to output
            out.write(genresJson.toString());

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

    }
}
