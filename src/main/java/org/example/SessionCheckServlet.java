package org.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "SessionCheckServlet", urlPatterns = {"/api/session-check"})
public class SessionCheckServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        // Debugging info
        System.out.println("Session exists: " + (session != null));
        if (session != null) {
            System.out.println("User in session: " + session.getAttribute("user"));
        }

        // Prevent caching so stale session info isn't reused
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Return correct JSON format
        response.getWriter().write("{\"loggedIn\": " + loggedIn + "}");
    }
}
