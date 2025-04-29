import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    @Resource(name = "jdbc/moviedbexample")
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String enteredPassword = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT password FROM customers WHERE email = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (validatePassword(enteredPassword, storedHash)) {
                    request.getSession().setAttribute("user", email);
                    response.sendRedirect("/project1/main.html");
                } else {
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\": \"fail\", \"message\": \"Invalid email or password.\"}");
                }
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"status\": \"fail\", \"message\": \"Invalid email or password.\"}");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
