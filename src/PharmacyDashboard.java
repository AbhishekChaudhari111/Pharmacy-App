import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class PharmacyDashboard extends JFrame {
    private Connection connection;
    private DefaultListModel<String> medicineListModel;
    private JList<String> medicineList;
    private JTextField searchField;
    private DefaultListModel<String> cartModel;
    private JList<String> cartList;
    private double totalAmount;
    private JLabel totalLabel;

    public PharmacyDashboard(Connection conn) {
        setTitle("Pharmacy Management System - Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        this.connection = conn;

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel();
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        topPanel.add(searchField);
        topPanel.add(searchButton);

        JButton adminOptionsButton = new JButton("Add Stock"); // Button for admin options
        topPanel.add(adminOptionsButton); // Add admin options button to the top panel

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        // Medicine List
        JPanel medicinePanel = new JPanel(new BorderLayout());
        JLabel medicineLabel = new JLabel("Medicines Available");
        medicinePanel.add(medicineLabel, BorderLayout.NORTH);
        medicineListModel = new DefaultListModel<>();
        medicineList = new JList<>(medicineListModel);
        medicineList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        medicinePanel.add(new JScrollPane(medicineList), BorderLayout.CENTER);
        centerPanel.add(medicinePanel);

        // Cart Panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        JLabel cartLabel = new JLabel("Cart");
        cartPanel.add(cartLabel, BorderLayout.NORTH);
        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        cartPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);
        centerPanel.add(cartPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total: ₹0.00");
        JButton checkoutButton = new JButton("Checkout");
        JButton removeButton = new JButton("Remove Selected");
        JButton clearButton = new JButton("Clear Cart");
        bottomPanel.add(totalLabel);
        bottomPanel.add(checkoutButton);
        bottomPanel.add(removeButton);
        bottomPanel.add(clearButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        fetchMedicineData();

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchMedicine();
            }
        });

        adminOptionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openAdminDashboard();
                dispose();
            }
        });

        medicineList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                addToCart();
            }
        });

        checkoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkout();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedMedicines();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearCart();
            }
        });
    }

    private void fetchMedicineData() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Medicine");

            while (rs.next()) {
                int id = rs.getInt("MedicineID");
                String name = rs.getString("Name");
                double price = rs.getDouble("Price");
                int quantity = rs.getInt("Quantity");
                medicineListModel.addElement(id + " - " + name + " - Available units: " + quantity + " - ₹" + price);
            }

            rs.close();
            stmt.close();
            System.out.println("Connected to database");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching medicine data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error fetching medicine data: " + e.getMessage());
        }
    }

    private void searchMedicine() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (!searchTerm.isEmpty()) {
            for (int i = 0; i < medicineListModel.size(); i++) {
                String medicine = medicineListModel.getElementAt(i).toLowerCase();
                if (medicine.contains(searchTerm)) {
                    medicineList.setSelectedIndex(i);
                    medicineList.ensureIndexIsVisible(i);
                    return;
                }
            }
            JOptionPane.showMessageDialog(null, "Medicine not found.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addToCart() {
        ArrayList<String> selectedMedicines = new ArrayList<>(medicineList.getSelectedValuesList());
        for (String medicine : selectedMedicines) {
            String selectedMedicine = JOptionPane.showInputDialog(null, "Enter number of units for " + medicine, "Add to Cart", JOptionPane.QUESTION_MESSAGE);
            if (selectedMedicine != null && !selectedMedicine.isEmpty()) {
                int unitsToAdd = Integer.parseInt(selectedMedicine);
                String[] parts = medicine.split(" - ");
                if (parts.length != 4) {
                    System.err.println("Invalid medicine format: " + medicine);
                    continue;
                }
                int medicineID = Integer.parseInt(parts[0]); // Parse medicine ID directly
                String[] priceParts = parts[3].split("\\₹");
                if (priceParts.length != 2) {
                    System.err.println("Invalid price format: " + parts[3]);
                    continue;
                }
                double price = Double.parseDouble(priceParts[1]);
                int availableUnits = Integer.parseInt(parts[2].split(": ")[1].split(" ")[0]); // Extract available units from the string
                if (unitsToAdd > availableUnits) {
                    JOptionPane.showMessageDialog(null, "Insufficient stock for " + parts[1] + ". Available units: " + availableUnits, "Error", JOptionPane.ERROR_MESSAGE);
                    continue; // Don't add to cart if stock is insufficient
                }
                double totalPrice = price * unitsToAdd;
                String cartEntry = medicineID + " - " + parts[1] + " - ₹" + price + " x " + unitsToAdd + " = ₹" + totalPrice;
                cartModel.addElement(cartEntry);
                totalAmount += totalPrice;
            }
        }
        updateTotalLabel();
    }


    private void removeSelectedMedicines() {
        ArrayList<String> selectedMedicines = new ArrayList<>(cartList.getSelectedValuesList());
        for (String medicine : selectedMedicines) {
            cartModel.removeElement(medicine);
            String[] parts = medicine.split(" - ");
            if (parts.length != 4) {
                System.err.println("Invalid medicine format: " + medicine);
                continue;
            }
            String[] priceParts = parts[3].split("\\₹");
            if (priceParts.length != 2) {
                System.err.println("Invalid price format: " + parts[3]);
                continue;
            }
            double price = Double.parseDouble(priceParts[1]);
            int units = Integer.parseInt(parts[3].substring(parts[3].indexOf("x") + 1, parts[3].indexOf("=")).trim());
            totalAmount -= price * units;
        }
        updateTotalLabel();
    }

    private void clearCart() {
        cartModel.clear();
        totalAmount = 0.0;
        updateTotalLabel();
    }

    private void checkout() {
        ArrayList<String> selectedMedicines = new ArrayList<String>();

        // Populate selectedMedicines with medicines from the cart
        for (int i = 0; i < cartModel.size(); i++) {
            selectedMedicines.add(cartModel.getElementAt(i));
        }

        for (String medicine : selectedMedicines) {
            System.out.println(medicine);
            String[] parts = medicine.split(" - ");
            if (parts.length != 3) {
                System.err.println("Invalid medicine format: " + medicine);
                continue;
            }
            int medicineID = Integer.parseInt(parts[0]);
            String[] unitsParts = parts[2].split(" ");
            if (unitsParts.length != 5) {
                System.err.println("Invalid units format: " + parts[3]);
                continue;
            }
            int unitsToRemove = Integer.parseInt(unitsParts[2]);
            try {
                System.out.println(unitsToRemove);
                System.out.println(medicineID);
                PreparedStatement stmt = connection.prepareStatement("UPDATE Medicine SET Quantity = Quantity - ? WHERE MedicineID = ?");
                stmt.setInt(1, unitsToRemove);
                stmt.setInt(2, medicineID);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Medicine quantity updated successfully.");
                } else {
                    System.err.println("Failed to update medicine quantity.");
                }
                stmt.close();
            } catch (SQLException ex) {
                System.err.println("Error updating medicine quantity: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        try {
            connection.commit();
        } catch (SQLException ex) {
            System.err.println("Error committing transaction: " + ex.getMessage());
        }
        JOptionPane.showMessageDialog(null, generateInvoice(), "Invoice", JOptionPane.INFORMATION_MESSAGE);
        clearCart();
        updateMedicineList();
    }





    private String generateInvoice() {
        StringBuilder invoice = new StringBuilder();
        invoice.append("Invoice\n\n");
        for (int i = 0; i < cartModel.size(); i++) {
            invoice.append(cartModel.get(i)).append("\n");
        }
        invoice.append("\nTotal amount: ₹").append(String.format("%.2f", totalAmount));
        return invoice.toString();
    }

    private void updateTotalLabel() {
        totalLabel.setText("Total: ₹" + String.format("%.2f", totalAmount));
    }

    private void openAdminDashboard() {
        // Open Admin Dashboard window
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@ABHISHEK:1522:XE", "SYSTEM", "abhishek123");
            new AdminDashboard(conn).setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateMedicineList() {
        medicineListModel.clear();
        fetchMedicineData();
    }

    public static void main(String[] args) {
        // Establish database connection
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@ABHISHEK:1522:XE", "SYSTEM", "abhishek123");
            SwingUtilities.invokeLater(() -> {
                new PharmacyDashboard(conn).setVisible(true);
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Database driver not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
