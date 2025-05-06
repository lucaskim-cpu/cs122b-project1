package org.example;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

import org.mindrot.jbcrypt.BCrypt;

@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login"})
public class LoginServlet extends HttpServlet {

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT password FROM customers WHERE email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                // Make sure it's a valid bcrypt hash before checking
                if (hashedPassword != null && hashedPassword.startsWith("$2a$")) {
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        HttpSession session = request.getSession();
                        session.setAttribute("user", email);
                        response.sendRedirect(request.getContextPath() + "/main.html");
                        return;
                    }
                }
            }

            // Invalid login
            response.sendRedirect(request.getContextPath() + "/login.html?error=Invalid+email+or+password");

        } catch (Exception e) {
            e.printStackTrace();
            // Handle errors gracefully, not with a 500 page
            response.sendRedirect(request.getContextPath() + "/login.html?error=Unexpected+error");
        }
    }
}
