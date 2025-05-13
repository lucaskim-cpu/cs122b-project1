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

@WebServlet(name = "SessionCheckServlet", urlPatterns = {"/_dashboard/check-session"})
public class SessionCheckServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        // Log request headers
        System.out.println("\n🔍 DEBUG: Session check request");
        System.out.println("🔍 Request URL: " + request.getRequestURL());
        System.out.println("🔍 Request method: " + request.getMethod());
        
        // Log all request headers
        System.out.println("🔍 Request headers:");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println("  - " + headerName + ": " + request.getHeader(headerName));
        }
        
        // Log cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            System.out.println("🔍 Cookies received:");
            for (Cookie cookie : cookies) {
                System.out.println("  - " + cookie.getName() + " = " + cookie.getValue());
            }
        } else {
            System.out.println("🔍 No cookies received");
        }
        
        // Get existing session
        HttpSession session = request.getSession(false);
        
        // Debug logging
        System.out.println("\n🔍 Session details:");
        System.out.println("🔍 Session exists: " + (session != null));
        if (session != null) {
            System.out.println("🔍 Session ID: " + session.getId());
            System.out.println("🔍 Session creation time: " + new java.util.Date(session.getCreationTime()));
            System.out.println("🔍 Session last accessed: " + new java.util.Date(session.getLastAccessedTime()));
            System.out.println("🔍 Session timeout: " + session.getMaxInactiveInterval() + " seconds");
            System.out.println("🔍 Employee attribute: " + session.getAttribute("employee"));
        }
        
        // Check if session exists and has employee attribute
        if (session != null && session.getAttribute("employee") != null) {
            System.out.println("✅ Session is valid");
            out.write("{\"status\":\"success\",\"message\":\"Session valid\"}");
        } else {
            System.out.println("❌ No valid session found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"status\":\"error\",\"message\":\"No valid session\"}");
        }
    }
}
