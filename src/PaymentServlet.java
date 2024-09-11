import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
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
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            // Parse the URL-encoded form data
            String paymentData = request.getParameter("paymentData");
            Map<String, String> formParams = new HashMap<>();
            for (String param : paymentData.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    formParams.put(key, value);
                }
            }

            String firstName = formParams.get("firstName");
            String lastName = formParams.get("lastName");
            String expirationDate = formParams.get("expirationDate");
            String creditCardNumber = formParams.get("creditCardNumber").replaceAll("\\s+", "");

            System.out.println("form data:");
            System.out.println(firstName);
            System.out.println(lastName);
            System.out.println(expirationDate);
            System.out.println(creditCardNumber);
            java.sql.Date sqlExpirationDate = java.sql.Date.valueOf(expirationDate);

            boolean paymentValid = false;
            conn = dataSource.getConnection();
            String query = "SELECT * FROM creditcards WHERE firstName = ? AND lastName = ? AND expiration = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setDate(3, sqlExpirationDate);

            rs = statement.executeQuery();
            System.out.println("Executed query");
            while (rs.next()) {
                String ccNumber = rs.getString("id").replaceAll("\\s+", "");
                if (ccNumber.equals(creditCardNumber)) {
                    paymentValid = true;
                    break;
                }
            }

            JsonObject responseJsonObject = new JsonObject();
            if (paymentValid) {
                System.out.println("Payment info valid");
                responseJsonObject.addProperty("success", true);
                responseJsonObject.addProperty("message", "Payment processed successfully.");

                String jsonArrayString = request.getParameter("cartItems");
                JsonArray movieIdsOfSales = JsonParser.parseString(jsonArrayString).getAsJsonArray();
                System.out.println("Cart: " + jsonArrayString);
                PreparedStatement insertStatement = null;
                try {
                    String insertQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?)";
                    insertStatement = conn.prepareStatement(insertQuery);
                    System.out.println("Prepared insert query");

                    String userIdString = (String) request.getSession().getAttribute("userId");
                    Integer userId = Integer.parseInt(userIdString);
                    System.out.println("Current user id: " + userId);
                    Date saleDate = new Date(System.currentTimeMillis());

                    for (JsonElement element : movieIdsOfSales) {
                        String movieId = element.getAsString();
                        insertStatement.setInt(1, userId);
                        insertStatement.setString(2, movieId);
                        insertStatement.setDate(3, saleDate);
                        System.out.println("Before updating table: ");
                        insertStatement.executeUpdate();
                        System.out.println("After updating table");
                    }
                } finally {
                    if (insertStatement != null) {
                        insertStatement.close();
                    }
                }
            } else {
                System.out.println("Invalid payment");
                responseJsonObject.addProperty("success", false);
                responseJsonObject.addProperty("message", "Payment processing failed.");
            }

            out.write(responseJsonObject.toString());
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            System.out.println("error:" + e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            out.close();
        }
    }
}