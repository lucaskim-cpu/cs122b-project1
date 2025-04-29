package org.example;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

// Removed @WebServlet annotation to rely solely on web.xml for servlet mapping.
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    private boolean validatePassword(String enteredPassword, String storedHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(enteredPassword.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedPassword) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return storedHash.equals(hexString.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() throws ServletException {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            dataSource = (DataSource) envCtx.lookup("jdbc/moviedbexample");
        } catch (NamingException e) {
            throw new ServletException("Cannot retrieve java:comp/env/jdbc/moviedbexample", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT password FROM customers WHERE email = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, email);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password");
                        if (validatePassword(password, storedHash)) {
                            request.getSession().setAttribute("user", email);
                            response.sendRedirect("/project1/main.html");
                        } else {
                            response.sendRedirect("/project1/login.html?error=Invalid%20credentials");
                        }
                    } else {
                        response.sendRedirect("/project1/login.html?error=Invalid%20credentials");
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
