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
        boolean isValidUser = "admin".equals(username) && "password".equals(password);
        
        if (isValidUser) {
            HttpSession session = request.getSession();
            session.setAttribute("user", username);
            response.sendRedirect("main.html");
        } else {
            response.sendRedirect("login.html?error=Invalid username or password");
        }
    }
} 