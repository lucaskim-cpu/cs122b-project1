package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.JSONObject;
import org.json.JSONException;

@WebServlet(name = "AddStarServlet", urlPatterns = {"/_dashboard/add-star"})
public class AddStarServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        // Check session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("❌ Unauthorized access attempt to add-star");
            out.write("{\"status\":\"error\",\"message\":\"Unauthorized access\"}");
            return;
        }
        
        try {
            // Parse JSON request body
            JSONObject jsonRequest = new JSONObject(request.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual));
            
            String name = jsonRequest.getString("name");
            Integer birthYear = jsonRequest.has("birthYear") && !jsonRequest.isNull("birthYear") 
                    ? jsonRequest.getInt("birthYear") 
                    : null;
            
            // Debug logging
            System.out.println("🔍 DEBUG: Adding new star");
            System.out.println("🔍 Name: " + name);
            System.out.println("🔍 Birth Year: " + birthYear);
            
            // Get database connection
            Connection conn = DatabaseConnection.getConnection();
            
            // Insert new star
            String sql = "INSERT INTO stars (name, birthYear) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                if (birthYear != null) {
                    stmt.setInt(2, birthYear);
                } else {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                }
                
                int result = stmt.executeUpdate();
                
                if (result > 0) {
                    System.out.println("✅ Star added successfully");
                    out.write("{\"status\":\"success\",\"message\":\"Star added successfully\"}");
                } else {
                    System.out.println("❌ Failed to add star");
                    out.write("{\"status\":\"error\",\"message\":\"Failed to add star\"}");
                }
            }
            
        } catch (JSONException e) {
            System.out.println("❌ Invalid JSON request: " + e.getMessage());
            out.write("{\"status\":\"error\",\"message\":\"Invalid request format\"}");
        } catch (Exception e) {
            System.out.println("❌ Error adding star: " + e.getMessage());
            out.write("{\"status\":\"error\",\"message\":\"Database error occurred\"}");
        }
    }
} 