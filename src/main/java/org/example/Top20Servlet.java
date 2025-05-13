package org.example;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;

import java.sql.*;

public class Top20Servlet extends HttpServlet {

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("🚫 Not logged in — rejecting /api/top20");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        System.out.println("✅ Session OK: " + session.getAttribute("user"));

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director, r.rating FROM movies m, ratings r WHERE m.id = r.movieId ORDER BY r.rating DESC LIMIT 20";


            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JSONArray movies = new JSONArray();

            while (rs.next()) {
                JSONObject movie = new JSONObject();
                movie.put("id", rs.getString("id"));
                movie.put("title", rs.getString("title"));
                movie.put("year", rs.getInt("year"));
                movie.put("director", rs.getString("director"));
                movie.put("rating", rs.getDouble("rating"));
                movies.put(movie);
            }

            out.write(movies.toString());
            response.setStatus(HttpServletResponse.SC_OK);

            rs.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}
