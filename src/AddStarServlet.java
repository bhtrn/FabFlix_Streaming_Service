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


@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add_star")
public class AddStarServlet extends HttpServlet {
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
        response.setContentType("application/json"); // Response will be in JSON format
        PrintWriter out = response.getWriter();

        try (out; Connection conn = dataSource.getConnection()) {
            // Retrieve form parameters
            String starName = request.getParameter("starName");
            String birthYearStr = request.getParameter("birthYear");
            Integer birthYear = null; // Use Integer to handle null birth year

            // Validate and convert birthYear to Integer if it's not empty
            if (birthYearStr != null && !birthYearStr.trim().isEmpty()) {
                try {
                    birthYear = Integer.parseInt(birthYearStr);
                } catch (NumberFormatException e) {
                    // Handle invalid birth year format
                    JsonObject responseJsonObject = new JsonObject();
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Birth year must be a valid integer.");
                    out.write(responseJsonObject.toString());
                    return;
                }
            }

            // Prepare SQL statement to call the stored procedure
            String query = "CALL add_star(?, ?);"; // This line replaces your INSERT statement
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, starName);
                if (birthYear != null) {
                    statement.setInt(2, birthYear);
                } else {
                    statement.setNull(2, java.sql.Types.INTEGER); // Handle null birth year
                }

                // Execute SQL statement
                int rowsAffected = statement.executeUpdate();

                // Prepare response JSON
                JsonObject responseJsonObject = new JsonObject();
                if (rowsAffected > 0) {
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Star added successfully.");
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Failed to add star.");
                }

                out.write(responseJsonObject.toString());
            }
        } catch (Exception e) {
            // Write error message JSON
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Error adding star: " + e.getMessage());
            out.write(responseJsonObject.toString());
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
}