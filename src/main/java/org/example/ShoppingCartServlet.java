package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@WebServlet("/api/cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Random RANDOM = new Random();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        Gson gson = new Gson();
        String json = gson.toJson(cart.values());

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, CartItem> cart = (Map<String, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        String action = request.getParameter("action");
        String itemName = request.getParameter("item");
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        switch (action) {
            case "add":
                cart.compute(itemName, (key, item) -> {
                    if (item == null) {
                        return new CartItem(itemName, quantity, 10 + RANDOM.nextInt(11));
                    } else {
                        item.setQuantity(item.getQuantity() + quantity);
                        return item;
                    }
                });
                break;
            case "update":
                if (cart.containsKey(itemName)) {
                    cart.get(itemName).setQuantity(quantity);
                }
                break;
            case "delete":
                cart.remove(itemName);
                break;
        }

        session.setAttribute("cart", cart);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public static class CartItem {
        private String name;
        private int quantity;
        private double price;

        public CartItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }
    }
}
