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

public class SearchServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        String startsWith = request.getParameter("startsWith");
        String genre = request.getParameter("genre");

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to establish a connection to the database.");
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT m.id, m.title, m.year, m.director, IFNULL(r.rating, 0.0) AS rating, ");
            query.append("GROUP_CONCAT(DISTINCT s.name SEPARATOR ', ') AS stars ");
            query.append("FROM movies m ");
            query.append("LEFT JOIN ratings r ON m.id = r.movieId ");
            query.append("LEFT JOIN stars_in_movies sim ON m.id = sim.movieId ");
            query.append("LEFT JOIN stars s ON sim.starId = s.id ");
            if (genre != null && !genre.isEmpty()) {
                query.append("JOIN genres_in_movies gim ON m.id = gim.movieId ");
                query.append("JOIN genres g ON gim.genreId = g.id ");
            }
            query.append("WHERE 1=1 ");

            if (title != null && !title.isEmpty()) {
                query.append("AND m.title LIKE ? ");
            }
            if (year != null && !year.isEmpty()) {
                query.append("AND m.year = ? ");
            }
            if (director != null && !director.isEmpty()) {
                query.append("AND m.director LIKE ? ");
            }
            if (star != null && !star.isEmpty()) {
                query.append("AND EXISTS (");
                query.append("SELECT 1 FROM stars s2 JOIN stars_in_movies sim2 ON s2.id = sim2.starId ");
                query.append("WHERE sim2.movieId = m.id AND s2.name LIKE ? ");
                query.append(") ");
            }
            if (startsWith != null && !startsWith.isEmpty()) {
                if (startsWith.equals("*")) {
                    query.append("AND m.title REGEXP '^[^A-Za-z0-9]' ");
                } else {
                    query.append("AND m.title LIKE ? ");
                }
            }
            if (genre != null && !genre.isEmpty()) {
                query.append("AND g.name = ? ");
            }

            query.append("GROUP BY m.id, m.title, m.year, m.director, r.rating ");
            query.append("ORDER BY m.title ASC LIMIT 100");

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
            if (startsWith != null && !startsWith.isEmpty() && !startsWith.equals("*")) {
                statement.setString(paramIndex++, startsWith + "%");
            }
            if (genre != null && !genre.isEmpty()) {
                statement.setString(paramIndex++, genre);
            }

            ResultSet rs = statement.executeQuery();
            JSONArray movies = new JSONArray();
            while (rs.next()) {
                JSONObject movie = new JSONObject();
                movie.put("title", rs.getString("title"));
                movie.put("year", rs.getInt("year"));
                movie.put("director", rs.getString("director"));
                movie.put("rating", rs.getDouble("rating"));
                movie.put("stars", rs.getString("stars") == null ? "" : rs.getString("stars"));
                movies.put(movie);
            }

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }
}
