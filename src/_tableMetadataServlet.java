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

import java.sql.DatabaseMetaData;


@WebServlet(name = "TableMetadataServlet", urlPatterns = "/api/_tableMetadata")
public class _tableMetadataServlet extends HttpServlet {
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
            System.out.println("Connected to database.");

            // Get the metadata of the connection
            DatabaseMetaData databaseMetaData = conn.getMetaData();

            // Get metadata of all tables in moviedb
            ResultSet tables = databaseMetaData.getTables("moviedb", null, "%", null);
            JsonArray tableMetadata = new JsonArray();

            // For each table returned
            while (tables.next()) {
                // Get table name [3rd parameter of .getTables()]
                String tableName = tables.getString(3);
                System.out.println("Processing table: " + tableName);

                // Create a JSON object to represent the table
                JsonObject tableObject = new JsonObject();
                tableObject.addProperty("table_name", tableName);

                // Get columns for each table
                ResultSet columns = databaseMetaData.getColumns("moviedb", null, tableName, "%");
                JsonArray columnMetadata = new JsonArray();

                // For each column in the table
                while (columns.next()) {
                    // Get the name of the column and its type.
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    System.out.println("Column: " + columnName + " Type: " + columnType);

                    JsonObject columnObject = new JsonObject();
                    columnObject.addProperty("column_name", columnName);
                    columnObject.addProperty("column_type", columnType);

                    columnMetadata.add(columnObject);
                }
                columns.close();

                tableObject.add("columns", columnMetadata);
                tableMetadata.add(tableObject);
            }
            tables.close();

            out.write(tableMetadata.toString());
            System.out.println("Metadata successfully retrieved and sent to client.");

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
            System.out.println("Error: " + e.getMessage());
        } finally {
            out.close();
        }
    }
}

