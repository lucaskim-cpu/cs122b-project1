package org.example;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class MovieDetailServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String movieId = request.getParameter("id");
        if (movieId == null || movieId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Missing movie ID.\"}");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director, IFNULL(r.rating, 0) AS rating, " +
                    "GROUP_CONCAT(DISTINCT s.name, ':', s.id SEPARATOR '|') AS stars " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars s ON sim.starId = s.id " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, movieId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JSONObject movie = new JSONObject();
                movie.put("id", rs.getString("id"));
                movie.put("title", rs.getString("title"));
                movie.put("year", rs.getInt("year"));
                movie.put("director", rs.getString("director"));
                movie.put("rating", rs.getDouble("rating"));

                JSONArray starsArray = new JSONArray();
                String starsData = rs.getString("stars");
                if (starsData != null) {
                    for (String entry : starsData.split("\\|")) {
                        String[] parts = entry.split(":");
                        JSONObject star = new JSONObject();
                        star.put("name", parts[0]);
                        star.put("id", parts[1]);
                        starsArray.put(star);
                    }
                }
                movie.put("stars", starsArray);

                out.write(movie.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\": \"Movie not found.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
