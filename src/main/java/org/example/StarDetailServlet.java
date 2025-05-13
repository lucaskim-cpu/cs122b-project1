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

public class StarDetailServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String starId = request.getParameter("id");
        if (starId == null || starId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Missing star ID.\"}");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT s.id, s.name, s.birthYear, " +
                    "GROUP_CONCAT(DISTINCT m.title, ':', m.id SEPARATOR '|') AS movies " +
                    "FROM stars s " +
                    "LEFT JOIN stars_in_movies sim ON s.id = sim.starId " +
                    "LEFT JOIN movies m ON sim.movieId = m.id " +
                    "WHERE s.id = ? GROUP BY s.id, s.name, s.birthYear";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, starId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JSONObject star = new JSONObject();
                star.put("id", rs.getString("id"));
                star.put("name", rs.getString("name"));
                star.put("birthYear", rs.getObject("birthYear"));

                JSONArray moviesArray = new JSONArray();
                String moviesData = rs.getString("movies");
                if (moviesData != null) {
                    for (String entry : moviesData.split("\\|")) {
                        String[] parts = entry.split(":");
                        JSONObject movie = new JSONObject();
                        movie.put("title", parts[0]);
                        movie.put("id", parts[1]);
                        moviesArray.put(movie);
                    }
                }
                star.put("movies", moviesArray);

                out.write(star.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\": \"Star not found.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
