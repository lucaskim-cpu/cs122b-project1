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
        
        // Hardcoded credentials check
        if ("test@email.com".equals(email) && "test123".equals(password)) {
            // Create new session
            HttpSession session = request.getSession(true);
            session.setAttribute("employee", email);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes
            
            // Debug session info
            System.out.println("[DEBUG] Created session with ID: " + session.getId());
            System.out.println("[DEBUG] Set attribute 'employee' to: " + email);
            System.out.println("[DEBUG] Session max inactive interval: " + session.getMaxInactiveInterval() + " seconds");
            
            // Set SameSite and Secure attributes for the session cookie
            // Ensure the session cookie is properly configured
            String sessionCookie = response.getHeader("Set-Cookie");
            System.out.println("[DEBUG] Original Set-Cookie header: " + sessionCookie);
            
            if (sessionCookie != null) {
                // Update the cookie with appropriate path, SameSite and Secure attributes
                if (sessionCookie.contains("Path=/")) {
                    // Replace default path with one that includes our application context
                    sessionCookie = sessionCookie.replace("Path=/", "Path=/fabflix");
                }
                String newCookie = sessionCookie + "; SameSite=None; Secure";
                response.setHeader("Set-Cookie", newCookie);
                System.out.println("[DEBUG] Modified Set-Cookie header: " + newCookie);
            } else {
                System.out.println("[DEBUG] WARNING: No Set-Cookie header found to modify!");
                
                // Set a cookie manually with the correct path
                Cookie cookie = new Cookie("JSESSIONID", session.getId());
                cookie.setPath("/fabflix");  // Important: set path to match application context
                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
                System.out.println("[DEBUG] Added manual cookie: JSESSIONID=" + session.getId() + " with path /fabflix");
            }
            
            // Return success response with redirect info
            System.out.println("[DEBUG] Login successful, returning success JSON response");
            out.write("{\"status\":\"success\",\"message\":\"Login successful\",\"redirect\":\"/fabflix/_dashboard/dashboard.html\"}");
        } else {
            // Return error response
            out.write("{\"status\":\"error\",\"message\":\"Invalid email or password\"}");
        }
    }
}