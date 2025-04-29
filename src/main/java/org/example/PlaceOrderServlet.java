package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/place-order")
public class PlaceOrderServlet extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            String insertSale = "INSERT INTO sales (movieId, quantity, price) VALUES (?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertSale)) {
                for (CartItem item : cart.values()) {
                    ps.setString(1, item.getMovieId());
                    ps.setInt(2, item.getQuantity());
                    ps.setDouble(3, item.getPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();

                session.removeAttribute("cart");
                request.getRequestDispatcher("/confirmation.html").forward(request, response);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            response.setContentType("text/html");
            response.getWriter().println("<html><body><h3>Order processing failed. Please try again.</h3></body></html>");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
} 