package org.example;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

@WebServlet(name = "AddStarServlet", urlPatterns = "/fabflix/_dashboard/add-star")
public class AddStarServlet extends HttpServlet {

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        // Check if user is logged in as employee
        HttpSession session = request.getSession();
        String employee = (String) session.getAttribute("employee");
        if (employee == null) {
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "Not authorized. Please log in as an employee.");
            out.write(responseJsonObject.toString());
            return;
        }

        try {
            // Parse the request body to extract star details
            StringBuilder requestBody = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
            
            Gson gson = new Gson();
            JsonObject jsonRequest = gson.fromJson(requestBody.toString(), JsonObject.class);

            String name = jsonRequest.get("name").getAsString();
            Integer birthYear = null;
            
            // Birth year is optional
            if (jsonRequest.has("birthYear") && !jsonRequest.get("birthYear").isJsonNull()) {
                try {
                    birthYear = jsonRequest.get("birthYear").getAsInt();
                } catch (NumberFormatException e) {
                    // If birth year is not a valid number, leave it as null
                }
            }

            // Log operation for debugging
            System.out.println("[DEBUG] Adding star: " + name + ", Birth Year: " + birthYear);

            // Validation
            if (name == null || name.trim().isEmpty()) {
                responseJsonObject.addProperty("status", "error");
                responseJsonObject.addProperty("message", "Star name cannot be empty");
                out.write(responseJsonObject.toString());
                return;
            }

            try (Connection conn = dataSource.getConnection()) {
                // Use a transaction to ensure data integrity
                conn.setAutoCommit(false);

                // Generate a new star ID
                String maxIdQuery = "SELECT MAX(id) AS max_id FROM stars";
                String newStarId = null;
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(maxIdQuery)) {
                    if (rs.next()) {
                        String maxId = rs.getString("max_id");
                        // If maxId starts with 'nm', extract the number part and increment
                        if (maxId != null && maxId.startsWith("nm")) {
                            String numericPart = maxId.substring(2);
                            int newNumber = Integer.parseInt(numericPart) + 1;
                            newStarId = "nm" + String.format("%07d", newNumber);
                        } else {
                            // Default if no existing stars or unexpected format
                            newStarId = "nm0000001";
                        }
                    } else {
                        newStarId = "nm0000001"; // Default if no stars exist
                    }
                }

                // Insert the new star
                String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, newStarId);
                    pstmt.setString(2, name);
                    
                    if (birthYear != null) {
                        pstmt.setInt(3, birthYear);
                    } else {
                        pstmt.setNull(3, Types.INTEGER);
                    }
                    
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        // Commit the transaction
                        conn.commit();
                        
                        // Return success response
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "Star added successfully");
                        responseJsonObject.addProperty("starId", newStarId);
                        responseJsonObject.addProperty("name", name);
                        if (birthYear != null) {
                            responseJsonObject.addProperty("birthYear", birthYear);
                        }
                    } else {
                        // Rollback if no rows were affected
                        conn.rollback();
                        responseJsonObject.addProperty("status", "error");
                        responseJsonObject.addProperty("message", "Failed to add star to database");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", "Error processing request: " + e.getMessage());
        }

        out.write(responseJsonObject.toString());
    }
}