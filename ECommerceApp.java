import java.sql.*;
import java.util.Scanner;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author PL
 */
public class ECommerceApp {
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser;

    public static void main(String[] args) {
        loadDriver();
        while (true) {
            System.out.println("Welcome to E-Commerce System");
            System.out.println("1. Sign Up");
            System.out.println("2. Sign In");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> signUp();
                case 2 -> signIn();
                case 3 -> {
                    System.out.println("Exiting the system. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/ecommerce";
        String user = "root";
        String password = "ariyan@378#";
        return DriverManager.getConnection(url, user, password);
    }

    private static void signUp() {
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Phone Number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String checkUserSql = "SELECT phone_number FROM Users WHERE phone_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkUserSql)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("User already exists with this phone number.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String insertUserSql = "INSERT INTO Users (phone_number, name, password, balance) VALUES (?, ?, ?, 0.0)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUserSql)) {
            stmt.setString(1, phoneNumber);
            stmt.setString(2, name);
            stmt.setString(3, password);
            stmt.executeUpdate();
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void signIn() {
        System.out.print("Enter Phone Number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        String sql = "SELECT name, password, balance FROM Users WHERE phone_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(password)) {
                    String name = rs.getString("name");
                    double balance = rs.getDouble("balance");
                    currentUser = new User(name, phoneNumber, password, balance);
                    System.out.println("Login successful. Welcome, " + currentUser.name);
                    homePage();
                } else {
                    System.out.println("Invalid password.");
                }
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void homePage() {
        while (true) {
            System.out.println("\nHome Page");
            System.out.println("1. Send Money");
            System.out.println("2. Add Money");
            System.out.println("3. View Transactions");
            System.out.println("4. Check Balance");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> sendMoney();
                case 2 -> addMoney();
                case 3 -> currentUser.showTransactions();
                case 4 -> checkBalance();
                case 5 -> {
                    currentUser = null;
                    System.out.println("Logged out successfully.");
                    return;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static void sendMoney() {
        if (currentUser == null) return;

        System.out.print("Enter recipient's phone number: ");
        String recipientPhone = scanner.nextLine();

        String checkRecipientSql = "SELECT phone_number FROM Users WHERE phone_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkRecipientSql)) {
            stmt.setString(1, recipientPhone);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Recipient not found.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        currentUser.sendMoney(recipientPhone, amount);
    }

    private static void addMoney() {
        if (currentUser == null) return;

        System.out.print("Enter amount to add: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        currentUser.addMoney(amount);
    }

    private static void checkBalance() {
        String sql = "SELECT balance FROM Users WHERE phone_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, currentUser.phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                System.out.println("Current Balance: $" + balance);
                currentUser.balance = balance; // Update local balance
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
