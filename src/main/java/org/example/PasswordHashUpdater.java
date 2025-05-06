package org.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class PasswordHashUpdater {

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/moviedb";
        String dbUser = "root"; // your DB user
        String dbPassword = "yourpassword"; // your DB password

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            // Step 1: Query customers with plaintext passwords (not starting with $2a$)
            String selectQuery = "SELECT id, password FROM customers WHERE password NOT LIKE '$2a$%'";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String rawPassword = rs.getString("password");
                String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

                // Step 2: Update the password in DB
                String updateQuery = "UPDATE customers SET password = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, hashed);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();

                System.out.println("Updated password for user ID: " + id);
                updateStmt.close();
            }

            rs.close();
            selectStmt.close();
            System.out.println("✅ All plaintext passwords have been hashed and updated.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
