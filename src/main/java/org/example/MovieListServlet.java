package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "MovieListServlet", urlPatterns = {"/api/top20"})
public class MovieListServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (request.getSession().getAttribute("user") == null) {
            response.sendRedirect("/project1/login.html");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.title, m.year, m.director, r.rating FROM movies m, ratings r WHERE m.id = r.movieId ORDER BY r.rating DESC LIMIT 20";
            PreparedStatement statement = conn.prepareStatement(query);
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
