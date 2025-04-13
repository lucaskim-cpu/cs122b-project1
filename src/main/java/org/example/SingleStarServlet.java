package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            String starName = request.getParameter("name");
            if (starName == null || starName.trim().isEmpty()) {
                throw new ServletException("Star name not provided");
            }

            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            Connection conn = ds.getConnection();

            // First get star details
            String starQuery = "SELECT s.* FROM stars s WHERE s.name = ?";
            PreparedStatement starStmt = conn.prepareStatement(starQuery);
            starStmt.setString(1, starName);
            ResultSet starRs = starStmt.executeQuery();

            JSONObject starJson = new JSONObject();
            if (starRs.next()) {
                starJson.put("id", starRs.getString("id"));
                starJson.put("name", starRs.getString("name"));
                starJson.put("birthYear", starRs.getInt("birthYear"));

                // Get movies for this star
                String movieQuery = "SELECT m.id, m.title, m.year, m.director " +
                        "FROM stars s " +
                        "JOIN stars_in_movies sim ON s.id = sim.starId " +
                        "JOIN movies m ON sim.movieId = m.id " +
                        "WHERE s.name = ?";
                
                PreparedStatement movieStmt = conn.prepareStatement(movieQuery);
                movieStmt.setString(1, starName);
                ResultSet movieRs = movieStmt.executeQuery();

                JSONArray movies = new JSONArray();
                while (movieRs.next()) {
                    JSONObject movie = new JSONObject();
                    movie.put("id", movieRs.getString("id"));
                    movie.put("title", movieRs.getString("title"));
                    movie.put("year", movieRs.getInt("year"));
                    movie.put("director", movieRs.getString("director"));
                    movies.put(movie);
                }
                starJson.put("movies", movies);

                movieRs.close();
                movieStmt.close();
            }

            starRs.close();
            starStmt.close();
            conn.close();

            out.write(starJson.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
} 