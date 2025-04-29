package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        JsonArray jsonArray = new JsonArray();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("movieId", entry.getKey());
            jsonObject.addProperty("quantity", entry.getValue());
            jsonArray.add(jsonObject);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonArray.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        String movieId = request.getParameter("movieId");
        String action = request.getParameter("action");
        
        if (movieId != null && action != null) {
            switch (action) {
                case "add":
                    cart.put(movieId, cart.getOrDefault(movieId, 0) + 1);
                    break;
                case "remove":
                    cart.remove(movieId);
                    break;
                case "update":
                    String quantityStr = request.getParameter("quantity");
                    if (quantityStr != null) {
                        int quantity = Integer.parseInt(quantityStr);
                        if (quantity > 0) {
                            cart.put(movieId, quantity);
                        } else {
                            cart.remove(movieId);
                        }
                    }
                    break;
            }
        }

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "Cart updated successfully");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
} 