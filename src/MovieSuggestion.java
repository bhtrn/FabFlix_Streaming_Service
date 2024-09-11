import com.google.gson.JsonArray;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@WebServlet("/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
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
    /*
     * 
     * Match the query against movie titles and return a JSON response.
     * 
     * For example, if the query is "super" for the super hero example:
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using.
     *
     * TODO: Make sure that your JSON results match this format
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();
			
			// get the query string from parameter
			String query = request.getParameter("query");
			System.out.println("Original Query: " + query);

			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}

			// Process query into keywords
			String[] keywords = query.split("\\s+");

			StringBuilder modifiedTitle = new StringBuilder();
			for (String keyword : keywords) {
				modifiedTitle.append(keyword).append("* ").append(" ");
			}

			query = modifiedTitle.toString().trim();
			System.out.println("Keyworded Query: " + query);

			// "Full-text" search the database and add the results to a JSON Array
			int maxSuggestions = 10;

			// Get suggestions from DB using query
			try (Connection conn = dataSource.getConnection()) {
				String sql_query =  "SELECT title, id " +
									"FROM movies " +
									"WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE) " +
									"LIMIT ?";

				PreparedStatement ps = conn.prepareStatement(sql_query);
				int paramIndex = 1;

				// Full-text match with the keyword prefixed query
				ps.setString(paramIndex++, query);

				// Set the max number of suggestions
				ps.setInt(paramIndex++, maxSuggestions);

				// Get the suggestsion results
				ResultSet rs = ps.executeQuery();

				// Processs all suggestions TODO: Double check format
				while (rs.next()) {
					// Get data
					String title = rs.getString("title");
					String id = rs.getString("id");

					// Add title as "value"
					JsonObject suggestion = new JsonObject();
					suggestion.addProperty("value", title);

					// Create a JsonObject to hold the id
					JsonObject id_obj = new JsonObject();
					id_obj.addProperty("movieId", id);

					// Add the data id into the suggestion JsonObject
					suggestion.add("data", id_obj);

					// Add the suggestion to the array of suggestions.
					jsonArray.add(suggestion);
				}

			}

			// Write the suggestions found
			response.getWriter().write(jsonArray.toString());
		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}
	
	/*
	 * Generate the JSON Object from hero to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "heroID": 11 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(Integer heroID, String heroName) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", heroName);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("heroID", heroID);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}


}
