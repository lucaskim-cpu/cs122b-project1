package org.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "EmployeeSession", urlPatterns = {"/api/employee-session"})
public class EmployeeSession extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        boolean isEmployeeLoggedIn = (session != null && session.getAttribute("employee") != null);
        
        // Debugging info
        System.out.println("Employee session check: Session exists = " + (session != null));
        if (session != null) {
            System.out.println("Employee in session: " + session.getAttribute("employee"));
        }
        
        // Prevent caching
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (isEmployeeLoggedIn) {
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Employee session valid\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"No valid employee session\"}");
        }
    }
}