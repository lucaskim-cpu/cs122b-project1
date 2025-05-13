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

@WebServlet(name = "DashboardLoginServlet", urlPatterns = {"/_dashboard/login"})
public class DashboardLoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        // Get parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Debug logging
        System.out.println("\n🔍 DEBUG: Login attempt");
        System.out.println("🔍 Email: " + email);
        System.out.println("🔍 Password: " + password);
        
        // Log existing cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            System.out.println("🔍 Existing cookies:");
            for (Cookie cookie : cookies) {
                System.out.println("  - " + cookie.getName() + " = " + cookie.getValue());
            }
        } else {
            System.out.println("🔍 No existing cookies");
        }
        
        // Hardcoded credentials check
        if ("test@email.com".equals(email) && "test123".equals(password)) {
            // Create new session
            HttpSession session = request.getSession(true);
            session.setAttribute("employee", email);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes
            
            // Debug logging
            System.out.println("\n✅ Login successful");
            System.out.println("✅ Session ID: " + session.getId());
            System.out.println("✅ Session created: " + (session.isNew() ? "Yes" : "No"));
            System.out.println("✅ Session timeout: " + session.getMaxInactiveInterval() + " seconds");
            
            // Set SameSite and Secure attributes for the session cookie
            String sessionCookie = response.getHeader("Set-Cookie");
            if (sessionCookie != null) {
                response.setHeader("Set-Cookie", sessionCookie + "; SameSite=None; Secure");
            }
            
            // Return success response
            out.write("{\"status\":\"success\",\"message\":\"Login successful\"}");
        } else {
            // Debug logging
            System.out.println("\n❌ Login failed - Invalid credentials");
            
            // Return error response
            out.write("{\"status\":\"error\",\"message\":\"Invalid email or password\"}");
        }
    }
} 