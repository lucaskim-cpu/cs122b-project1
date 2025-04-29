package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    @Override
    public void init() throws ServletException {
        try {
            InitialContext ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            throw new ServletException("Cannot retrieve java:comp/env/jdbc/moviedbexample", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
        double total = cart.values().stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();

        Gson gson = new Gson();
        String json = gson.toJson(Map.of("total", total));

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNumber = request.getParameter("creditCardNumber");
        String expiration = request.getParameter("expiration");

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM creditcards WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, creditCardNumber);
                ps.setString(4, expiration);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        request.getRequestDispatcher("/api/place-order").forward(request, response);
                    } else {
                        response.setContentType("text/html");
                        PrintWriter out = response.getWriter();
                        out.println("<html><body><h3>Invalid credit card information. Please try again.</h3></body></html>");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Database error during payment processing", e);
        }
    }
} 