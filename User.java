import java.sql.*;



/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author PL
 */
public class User {
    String name;
    String phoneNumber;
    String password;
    double balance;

    public User(String name, String phoneNumber, String password, double balance) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.balance = balance;
    }

    public void sendMoney(String receiverPhone, double amount) {
        Connection conn = null;
        try {
            conn = ECommerceApp.getConnection();
            conn.setAutoCommit(false);

            // Check sender's balance
            String checkBalanceSql = "SELECT balance FROM Users WHERE phone_number = ? FOR UPDATE";
            PreparedStatement checkStmt = conn.prepareStatement(checkBalanceSql);
            checkStmt.setString(1, this.phoneNumber);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                if (currentBalance < amount) {
                    System.out.println("Insufficient balance.");
                    conn.rollback();
                    return;
                }
            } else {
                System.out.println("Sender not found.");
                conn.rollback();
                return;
            }

            // Deduct from sender
            String updateSenderSql = "UPDATE Users SET balance = balance - ? WHERE phone_number = ?";
            PreparedStatement senderStmt = conn.prepareStatement(updateSenderSql);
            senderStmt.setDouble(1, amount);
            senderStmt.setString(2, this.phoneNumber);
            senderStmt.executeUpdate();

            // Add to receiver
            String updateReceiverSql = "UPDATE Users SET balance = balance + ? WHERE phone_number = ?";
            PreparedStatement receiverStmt = conn.prepareStatement(updateReceiverSql);
            receiverStmt.setDouble(1, amount);
            receiverStmt.setString(2, receiverPhone);
            int receiverUpdated = receiverStmt.executeUpdate();
            if (receiverUpdated == 0) {
                System.out.println("Recipient not found.");
                conn.rollback();
                return;
            }

            // Record transaction
            String insertTransactionSql = "INSERT INTO Transactions (sender_phone, receiver_phone, amount) VALUES (?, ?, ?)";
            PreparedStatement transactionStmt = conn.prepareStatement(insertTransactionSql);
            transactionStmt.setString(1, this.phoneNumber);
            transactionStmt.setString(2, receiverPhone);
            transactionStmt.setDouble(3, amount);
            transactionStmt.executeUpdate();

            conn.commit();

            // Update local balance
            this.balance -= amount;
            System.out.println("Money sent successfully.");
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error sending money: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void addMoney(double amount) {
        Connection conn = null;
        try {
            conn = ECommerceApp.getConnection();
            conn.setAutoCommit(false);

            // Update balance
            String updateSql = "UPDATE Users SET balance = balance + ? WHERE phone_number = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setDouble(1, amount);
            stmt.setString(2, this.phoneNumber);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                System.out.println("User not found.");
                conn.rollback();
                return;
            }

            // Record transaction
            String insertTransactionSql = "INSERT INTO Transactions (sender_phone, receiver_phone, amount) VALUES (NULL, ?, ?)";
            PreparedStatement transactionStmt = conn.prepareStatement(insertTransactionSql);
            transactionStmt.setString(1, this.phoneNumber);
            transactionStmt.setDouble(2, amount);
            transactionStmt.executeUpdate();

            conn.commit();

            // Update local balance
            this.balance += amount;
            System.out.println("Money added successfully.");
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error adding money: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void showTransactions() {
        String sql = "SELECT * FROM Transactions WHERE sender_phone = ? OR receiver_phone = ? ORDER BY transaction_date DESC";
        try (Connection conn = ECommerceApp.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, this.phoneNumber);
            stmt.setString(2, this.phoneNumber);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\nTransactions:");
            while (rs.next()) {
                String sender = rs.getString("sender_phone");
                String receiver = rs.getString("receiver_phone");
                double amount = rs.getDouble("amount");
                Timestamp date = rs.getTimestamp("transaction_date");

                if (sender == null) {
                    System.out.printf("[%s] Deposit: +$%.2f%n", date, amount);
                } else if (sender.equals(this.phoneNumber)) {
                    System.out.printf("[%s] Sent to %s: -$%.2f%n", date, receiver, amount);
                } else {
                    System.out.printf("[%s] Received from %s: +$%.2f%n", date, sender, amount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
