package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A servlet that handles HTTP GET requests to the /form url.
 * @author runhaoz
 * @version 1.0
 * @date 24/10/16
 */
// Removed @WebServlet annotation to rely solely on web.xml for servlet mapping.
public class BrowseServlet extends HttpServlet {
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String initial = request.getParameter("initial");

        try (Connection conn = dataSource.getConnection()) {
            StringBuilder query = new StringBuilder("SELECT m.title, m.year, m.director, r.rating FROM movies m, ratings r WHERE m.id = r.movieId");

            if (initial != null && !initial.isEmpty()) {
                if (initial.equals("*")) {
                    query.append(" AND m.title REGEXP '^[^a-zA-Z0-9]'");
                } else {
                    query.append(" AND m.title LIKE ?");
                }
            }

            PreparedStatement statement = conn.prepareStatement(query.toString());
            if (initial != null && !initial.isEmpty() && !initial.equals("*")) {
                statement.setString(1, initial + "%");
            }

            ResultSet rs = statement.executeQuery();
            JSONArray movies = new JSONArray();

            while (rs.next()) {
                JSONObject movie = new JSONObject();
                movie.put("title", rs.getString("title"));
                movie.put("year", rs.getInt("year"));
                movie.put("director", rs.getString("director"));
                movie.put("rating", rs.getDouble("rating"));
                movies.put(movie);
            }

            rs.close();
            statement.close();

            out.write(movies.toString());
            response.setStatus(200);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
