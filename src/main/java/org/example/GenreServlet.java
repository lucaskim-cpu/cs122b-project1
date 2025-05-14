package org.example;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GenreServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[DEBUG] GenreServlet - doGet called");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String sql = "SELECT name FROM genres ORDER BY name ASC";
        JSONArray genresArray = new JSONArray();
        System.out.println("[DEBUG] GenreServlet - DataSource is null? " + (dataSource == null));
        System.out.println("[DEBUG] GenreServlet - Executing SQL: " + sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int genreCount = 0;
            while (rs.next()) {
                String genreName = rs.getString("name");
                genresArray.put(genreName);
                genreCount++;
                System.out.println("[DEBUG] GenreServlet - Found genre: " + genreName);
            }
            System.out.println("[DEBUG] GenreServlet - Total genres found: " + genreCount);

            out.write(genresArray.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to retrieve genres\"}");
        } finally {
            out.close();
        }
    }
}
