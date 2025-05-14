package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "DashboardLoginServlet", urlPatterns = {"/fabflix/_dashboard/login"})
public class DashboardLoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Debug logging
        System.out.println("\n[DEBUG] DashboardLoginServlet - doPost called");
        System.out.println("[DEBUG] Request URL: " + request.getRequestURL());
        System.out.println("[DEBUG] Context Path: " + request.getContextPath());
        System.out.println("[DEBUG] Servlet Path: " + request.getServletPath());
        
        // Always set JSON content type
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        // Get parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        System.out.println("[DEBUG] Login attempt with email: " + email);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT password FROM employees WHERE email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
                boolean loginSuccess = passwordEncryptor.checkPassword(password, storedPassword);

                if (loginSuccess) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute("employee", email);
                    session.setMaxInactiveInterval(30 * 60); // 30 minutes

                    // Set SameSite and Secure attributes for the session cookie
                    Cookie cookie = new Cookie("JSESSIONID", session.getId());
                    cookie.setPath("/fabflix");  // Important: set path to match application context
                    cookie.setSecure(true);
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);

                    out.write("{\"status\":\"success\",\"message\":\"Login successful\",\"redirect\":\"/fabflix/_dashboard/dashboard.html\"}");
                } else {
                    out.write("{\"status\":\"error\",\"message\":\"Invalid email or password\"}");
                }

            } else {
                out.write("{\"status\":\"error\",\"message\":\"Invalid email or password\"}");
            }

            rs.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            out.write("{\"status\":\"error\",\"message\":\"Login failed due to server error\"}");
        }
    }
}