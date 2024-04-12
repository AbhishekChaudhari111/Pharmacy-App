import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PharmacyLogin extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public PharmacyLogin() {
        setTitle("Pharmacy Management - Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null); // Use null layout

        JLabel titleLabel = new JLabel("Pharmacy Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(80, 20, 250, 30);
        panel.add(titleLabel);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 80, 80, 20);
        panel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(150, 80, 200, 25);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 120, 80, 20);
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 120, 200, 25);
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(150, 160, 100, 30);
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = String.valueOf(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (!isValidLogin(username, password)) {
                    JOptionPane.showMessageDialog(null, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    openDashboard();
                }
            }
        });
    }

    private boolean isValidLogin(String username, String password) {
        // You can implement your authentication logic here
        // For simplicity, let's consider a hardcoded username and password
        return username.equals("admin") && password.equals("admin");
    }

    public void openDashboard() {
        // Open Dashboard window
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@ABHISHEK:1522:XE", "SYSTEM", "abhishek123");
            new PharmacyDashboard(conn).setVisible(true);
            dispose(); // Close login window
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PharmacyLogin().setVisible(true);
            }
        });
    }
}
