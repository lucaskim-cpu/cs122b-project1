package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "SessionCheckServlet", urlPatterns = {"/fabflix/_dashboard/check-session"})
public class SessionCheckServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Debug logging
        System.out.println("\n[DEBUG] SessionCheckServlet - doGet called");
        System.out.println("[DEBUG] Request URL: " + request.getRequestURL());
        System.out.println("[DEBUG] Context Path: " + request.getContextPath());
        System.out.println("[DEBUG] Servlet Path: " + request.getServletPath());
        
        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        // Get existing session
        HttpSession session = request.getSession(false);
        
        // Debug session details
        if (session != null) {
            System.out.println("[DEBUG] Found existing session with ID: " + session.getId());
            System.out.println("[DEBUG] Session creation time: " + new java.util.Date(session.getCreationTime()));
            System.out.println("[DEBUG] Session employee attribute: " + session.getAttribute("employee"));
            System.out.println("[DEBUG] Session user attribute: " + session.getAttribute("user"));
            
            // List all attribute names in session
            System.out.print("[DEBUG] All session attributes: ");
            java.util.Enumeration<String> attributeNames = session.getAttributeNames();
            if (!attributeNames.hasMoreElements()) {
                System.out.println("(none)");
            } else {
                while (attributeNames.hasMoreElements()) {
                    String name = attributeNames.nextElement();
                    System.out.print(name + "=" + session.getAttribute(name));
                    if (attributeNames.hasMoreElements()) System.out.print(", ");
                }
                System.out.println();
            }
        } else {
            System.out.println("[DEBUG] No existing session found");
        }
        
        // Check if session exists and has employee attribute
        if (session != null && session.getAttribute("employee") != null) {
            out.write("{\"status\":\"success\",\"message\":\"Session valid\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"status\":\"error\",\"message\":\"No valid session\"}");
        }
    }
}
