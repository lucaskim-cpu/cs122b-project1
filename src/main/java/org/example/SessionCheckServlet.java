package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

@WebServlet(name = "SessionCheckServlet", urlPatterns = {
    "/api/session-check",
    "/fabflix/_dashboard/check-session"
})
public class SessionCheckServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        HttpSession session = request.getSession(false);

        // Prevent caching
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // Debug logging
        System.out.println("[DEBUG] SessionCheckServlet called: " + path);
        if (session != null) {
            System.out.println("[DEBUG] Session ID: " + session.getId());
            System.out.println("[DEBUG] Attributes:");
            Enumeration<String> attributes = session.getAttributeNames();
            while (attributes.hasMoreElements()) {
                String attr = attributes.nextElement();
                System.out.println("    " + attr + " = " + session.getAttribute(attr));
            }
        } else {
            System.out.println("[DEBUG] No session found.");
        }

        // Route-specific logic
        if ("/fabflix/_dashboard/check-session".equals(path)) {
            // Employee session check
            if (session != null && session.getAttribute("userType") != null &&
                    session.getAttribute("userType").equals("employee")) {
                out.write("{\"status\":\"success\",\"message\":\"Employee session valid\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"status\":\"error\",\"message\":\"No valid employee session\"}");
            }

        } else {
            // Customer session check
            boolean loggedIn = (session != null && session.getAttribute("user") != null &&
                                "customer".equals(session.getAttribute("userType")));
            out.write("{\"loggedIn\": " + loggedIn + "}");
        }
    }
}
