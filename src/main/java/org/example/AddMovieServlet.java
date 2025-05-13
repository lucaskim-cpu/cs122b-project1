package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import org.json.JSONObject;
import org.json.JSONException;

@WebServlet(name = "AddMovieServlet", urlPatterns = {"/fabflix/_dashboard/add-movie"})
public class AddMovieServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        // Check session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("employee") == null) {
            System.out.println("❌ Unauthorized access attempt to add-movie");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"status\":\"error\",\"message\":\"Unauthorized access\"}");
            return;
        }
        
        try {
            // Parse JSON request body
            JSONObject jsonRequest = new JSONObject(request.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual));
            
            String title = jsonRequest.getString("title");
            int year = jsonRequest.getInt("year");
            String director = jsonRequest.getString("director");
            String starName = jsonRequest.getString("starName");
            String genreName = jsonRequest.getString("genreName");
            
            // Log request details
            System.out.println("🔍 DEBUG: Adding new movie");
            System.out.println("🔍 Title: " + title);
            System.out.println("🔍 Year: " + year);
            System.out.println("🔍 Director: " + director);
            System.out.println("🔍 Star: " + starName);
            System.out.println("🔍 Genre: " + genreName);
            
            // Get database connection
            Connection conn = DatabaseConnection.getConnection();
            
            // Call the stored procedure
            String callProcedure = "{CALL add_movie(?, ?, ?, ?, ?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(callProcedure)) {
                // Set input parameters
                stmt.setString(1, title);
                stmt.setInt(2, year);
                stmt.setString(3, director);
                stmt.setString(4, starName);
                stmt.setString(5, genreName);
                
                // Register output parameter
                stmt.registerOutParameter(6, Types.VARCHAR);
                
                // Execute the stored procedure
                stmt.execute();
                
                // Get the output message
                String resultMessage = stmt.getString(6);
                
                // Check if the operation was successful based on the message
                boolean isSuccess = !resultMessage.contains("already exists") && !resultMessage.contains("Error");
                
                if (isSuccess) {
                    System.out.println("✅ " + resultMessage);
                    out.write("{\"status\":\"success\",\"message\":\"" + resultMessage + "\"}");
                } else {
                    System.out.println("⚠️ " + resultMessage);
                    out.write("{\"status\":\"error\",\"message\":\"" + resultMessage + "\"}");
                }
            }
            
        } catch (JSONException e) {
            System.out.println("❌ Invalid JSON request: " + e.getMessage());
            out.write("{\"status\":\"error\",\"message\":\"Invalid request format\"}");
        } catch (Exception e) {
            System.out.println("❌ Error adding movie: " + e.getMessage());
            e.printStackTrace();
            
            String errorMsg = e.getMessage();
            if (errorMsg.contains("Parameter number 6 is not an OUT parameter") || 
                errorMsg.contains("PROCEDURE moviedb.add_movie does not exist")) {
                out.write("{\"status\":\"error\",\"message\":\"The stored procedure 'add_movie' needs to be created in the database first. Please run the stored-procedure.sql script.\"}");
            } else {
                out.write("{\"status\":\"error\",\"message\":\"Database error occurred: " + errorMsg + "\"}");                
            }
        }
    }
}
