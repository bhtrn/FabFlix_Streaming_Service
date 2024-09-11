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

// SessionEntriesUpdateServlet.java
@WebServlet(name = "SessionEntriesUpdate", urlPatterns = "/api/SessionEntriesUpdate")
public class SessionEntriesUpdateServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer entriesPerPage = Integer.valueOf(request.getParameter("entriesPerPage"));
            request.getSession().setAttribute("entriesPerPage", entriesPerPage);
            response.getWriter().write("Session updated with entriesPerPage: " + entriesPerPage);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid entries per page value.");
        }
    }
}