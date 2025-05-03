package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // TODO: Replace with actual authentication logic
        boolean isValidUser = "a2@email.com".equals(username) && "a2".equals(password);
        
        if (isValidUser) {
            HttpSession session = request.getSession();
            session.setAttribute("user", username);
            response.sendRedirect(request.getContextPath() + "/main.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/login.html?error=Invalid+username+or+password");
        }
    }
} 