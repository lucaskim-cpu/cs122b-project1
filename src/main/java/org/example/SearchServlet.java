package org.example;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A servlet that handles HTTP POST requests to the /api/search endpoint.
 */
// Removed @WebServlet annotation to rely solely on web.xml for servlet mapping.
public class SearchServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * Handles HTTP POST requests to the /api/search endpoint.
     *
     * @param request  the HTTP request object
     * @param response the HTTP response object
     * @throws ServletException if there is a servlet-related error
     * @throws IOException      if there is an I/O error
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (request.getSession().getAttribute("user") == null) {
            response.sendRedirect("/login.html");
            return;
        }

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to establish a connection to the database.");
            }
            StringBuilder query = new StringBuilder("SELECT m.title, m.year, m.director, r.rating FROM movies m, ratings r WHERE m.id = r.movieId");

            if (title != null && !title.isEmpty()) {
                query.append(" AND m.title LIKE ?");
            }
            if (year != null && !year.isEmpty()) {
                query.append(" AND m.year = ?");
            }
            if (director != null && !director.isEmpty()) {
                query.append(" AND m.director LIKE ?");
            }
            if (star != null && !star.isEmpty()) {
                query.append(" AND EXISTS (SELECT 1 FROM stars s, stars_in_movies sim WHERE s.id = sim.starId AND sim.movieId = m.id AND s.name LIKE ?)");
            }

            PreparedStatement statement = conn.prepareStatement(query.toString());
            int paramIndex = 1;
            if (title != null && !title.isEmpty()) {
                statement.setString(paramIndex++, "%" + title + "%");
            }
            if (year != null && !year.isEmpty()) {
                statement.setInt(paramIndex++, Integer.parseInt(year));
            }
            if (director != null && !director.isEmpty()) {
                statement.setString(paramIndex++, "%" + director + "%");
            }
            if (star != null && !star.isEmpty()) {
                statement.setString(paramIndex++, "%" + star + "%");
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
            JSONObject error = new JSONObject();
            error.put("errorMessage", e.getMessage());
            out.write(error.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
