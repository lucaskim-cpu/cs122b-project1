package org.example;

import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.*;

public class PasswordHashUpdater {

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/moviedb";
        String dbUser = "mytestuser"; // ✅ Replace with your DB user
        String dbPassword = "My6$Password"; // ✅ Replace with your DB password

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            // Step 1: Query customers with plaintext passwords (not starting with $2a$)
            String selectQuery = "SELECT id, password FROM customers WHERE LENGTH(password) < 60";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            ResultSet rs = selectStmt.executeQuery();

            StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

            while (rs.next()) {
                int id = rs.getInt("id");
                String rawPassword = rs.getString("password");

                // ✅ Hash the plain password using StrongPasswordEncryptor
                String hashed = encryptor.encryptPassword(rawPassword);

                // Step 2: Update the password in DB
                String updateQuery = "UPDATE customers SET password = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, hashed);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();

                System.out.println("✅ Updated password for user ID: " + id);
                updateStmt.close();
            }

            rs.close();
            selectStmt.close();
            System.out.println("🎉 All plaintext passwords have been hashed and updated.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
