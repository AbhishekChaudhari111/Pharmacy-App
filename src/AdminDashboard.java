import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private Connection connection;
    private JTextField medicineIdField;
    private JTextField medicineNameField;
    private JTextField medicinePriceField;
    private JTextField medicineQuantityField;

    public AdminDashboard(Connection conn) {
        setTitle("Admin Dashboard");
        setSize(400, 250);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        this.connection = conn;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 10, 10)); // Set GridLayout with 5 rows and 2 columns with gaps of 10 pixels

        JLabel idLabel = new JLabel("Medicine ID:");
        panel.add(idLabel);

        medicineIdField = new JTextField();
        panel.add(medicineIdField);

        JLabel nameLabel = new JLabel("Medicine Name:");
        panel.add(nameLabel);

        medicineNameField = new JTextField();
        panel.add(medicineNameField);

        JLabel priceLabel = new JLabel("Medicine Price:");
        panel.add(priceLabel);

        medicinePriceField = new JTextField();
        panel.add(medicinePriceField);

        JLabel quantityLabel = new JLabel("Medicine Quantity:");
        panel.add(quantityLabel);

        medicineQuantityField = new JTextField();
        panel.add(medicineQuantityField);

        JButton addButton = new JButton("Add Medicine");
        panel.add(addButton);

        JButton closeButton = new JButton("Close");
        panel.add(closeButton);

        add(panel);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addMedicine();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                PharmacyLogin l = new PharmacyLogin();
                l.openDashboard();
                PharmacyDashboard pd = new PharmacyDashboard(conn);// Close the admin dashboard window
            }
        });
    }

    private void addMedicine() {
        String idStr = medicineIdField.getText().trim();
        String name = medicineNameField.getText().trim();
        String priceStr = medicinePriceField.getText().trim();
        String quantityStr = medicineQuantityField.getText().trim();

        if (idStr.isEmpty() || name.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter medicine ID, name, price, and quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            double price = Double.parseDouble(priceStr);
            int quantity = Integer.parseInt(quantityStr);

            PreparedStatement stmt = connection.prepareStatement("INSERT INTO Medicine (medicineid, name, price, quantity) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setDouble(3, price);
            stmt.setInt(4, quantity);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Medicine added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
            } else {
                JOptionPane.showMessageDialog(null, "Failed to add medicine.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            stmt.close();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid ID, price, or quantity format. Please enter valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error adding medicine: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        medicineIdField.setText("");
        medicineNameField.setText("");
        medicinePriceField.setText("");
        medicineQuantityField.setText("");
    }
}
