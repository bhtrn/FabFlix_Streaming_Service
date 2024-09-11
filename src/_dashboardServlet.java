
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "_dashboardServlet", urlPatterns = {"/api/_dashboard", "/_dashboard"})
public class _dashboardServlet extends HttpServlet {
    //TODO: What does "serialVersionUID" mean/do?
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Check if the user is already logged in
        User user = (User) request.getSession().getAttribute("employee");
        if (user != null) {
            // User is logged in, redirect to the employee dashboard page
            response.sendRedirect("_employee_dashboard.html");
        } else {
            // User is not logged in, serve the dashboard page where they can login
            request.getRequestDispatcher("/_dashboard.html").forward(request, response);
        }
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        request.getServletContext().log("getting employee username: " + username + "with password: " + password);

        PrintWriter out = response.getWriter();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
        try (Connection conn = dataSource.getConnection()) {
            boolean reCaptchaSuccess;
            try {
                // Attempt to verify the reCAPTCHA response
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                reCaptchaSuccess = true; // reCAPTCHA verification succeeded
            } catch (Exception e) {
                // reCAPTCHA verification failed
                reCaptchaSuccess = false;
            }

            System.out.println("Passed recaptcha verification:" + reCaptchaSuccess);
            if (reCaptchaSuccess) {
                String query = "SELECT email, password, fullname FROM employees WHERE email = ? ";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, username);

                ResultSet rs = statement.executeQuery();

                JsonObject responseJson = new JsonObject();

                // If the username exists within our database, check if the password is correct
                if (rs.next()) {
                    String encryptedPassword = rs.getString("password");
                    // Use StrongPasswordEncryptor to check the provided password against the encrypted one
                    boolean passwordVerified = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

                    if (passwordVerified) {
                        // Add user to session
                        request.getSession().setAttribute("employee", new User(username));
                        request.getSession().setAttribute("employee_fullname", rs.getString("fullname"));
                        // Note that login was successful
                        responseJson.addProperty("status", "success");
                        responseJson.addProperty("message", "success");
                    } else {
                        // Login fail
                        responseJson.addProperty("status", "fail");
                        request.getServletContext().log("Login failed");
                        responseJson.addProperty("message", "incorrect username or password");
                    }
                } else {
                    // Login fail
                    responseJson.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Login failed");
                    responseJson.addProperty("message", "incorrect username or password");

                }

                rs.close();
                statement.close();

                out.write(responseJson.toString());
            }
            else {
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                responseJson.addProperty("message", "failed captcha, please try again");
                out.write(responseJson.toString());
            }
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

    }
}

