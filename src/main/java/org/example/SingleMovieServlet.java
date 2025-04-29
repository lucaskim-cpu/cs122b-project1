package org.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String movieId = request.getParameter("id");
            if (movieId == null || movieId.trim().isEmpty()) {
                throw new ServletException("Movie ID not provided");
            }

            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            Connection conn = ds.getConnection();

            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "GROUP_CONCAT(DISTINCT g.name) AS genres, " +
                    "GROUP_CONCAT(DISTINCT s.name) AS stars " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres g ON gim.genreId = g.id " +
                    "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                    "LEFT JOIN stars s ON sim.starId = s.id " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, movieId);
            ResultSet rs = stmt.executeQuery();

            JSONObject movieJson = new JSONObject();
            if (rs.next()) {
                movieJson.put("id", rs.getString("id"));
                movieJson.put("title", rs.getString("title") != null ? rs.getString("title") : "N/A");
                movieJson.put("year", rs.getObject("year") != null ? rs.getInt("year") : 0);
                movieJson.put("director", rs.getString("director") != null ? rs.getString("director") : "N/A");
                movieJson.put("rating", rs.getObject("rating") != null ? rs.getFloat("rating") : 0.0);

                String[] genres = rs.getString("genres") != null ?
                        rs.getString("genres").split(",") : new String[0];
                String[] stars = rs.getString("stars") != null ?
                        rs.getString("stars").split(",") : new String[0];

                movieJson.put("genres", genres);
                movieJson.put("stars", stars);
            }

            rs.close();
            stmt.close();
            conn.close();

            out.write(movieJson.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("errorMessage", e.getMessage());
            out.write(error.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
