package org.example;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (request.getSession().getAttribute("user") == null) {
            response.sendRedirect("/project1/login.html");
            return;
        }

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        try (Connection conn = dataSource.getConnection()) {
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
                query.append(" AND EXISTS (SELECT 1 FROM stars s, stars_in_movies sm WHERE s.id = sm.starId AND sm.movieId = m.id AND s.name LIKE ?)");
            }

            PreparedStatement statement = conn.prepareStatement(query.toString());
            int index = 1;
            if (title != null && !title.isEmpty()) {
                statement.setString(index++, "%" + title + "%");
            }
            if (year != null && !year.isEmpty()) {
                statement.setString(index++, year);
            }
            if (director != null && !director.isEmpty()) {
                statement.setString(index++, "%" + director + "%");
            }
            if (star != null && !star.isEmpty()) {
                statement.setString(index++, "%" + star + "%");
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
