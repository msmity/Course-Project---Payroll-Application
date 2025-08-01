package main.Admin;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import main.Database.DatabaseHandler;
import main.Login.Security;

public class EmployeeManagement {
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private java.util.List<String> userIDs;

    public void displayEmployeeManagement() {
        JFrame frame = new JFrame("User Management");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel, frame);

        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel, JFrame frame) {
        panel.setLayout(new BorderLayout());
    
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        JLabel searchLabel = new JLabel("Search by User ID, Name, or Job Title:");
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
    
        // User List
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane scrollPane = new JScrollPane(userList);
    
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
    
        // Load Users
        loadUsers("");
    
        // Add search functionality
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                loadUsers(searchField.getText());
            }
    
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                loadUsers(searchField.getText());
            }
    
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                loadUsers(searchField.getText());
            }
        });
    
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton editButton = new JButton("Edit Selected User");
        JButton createButton = new JButton("Add New User");
        JButton deleteButton = new JButton("Delete Selected User");
        JButton payTypeScreenButton = new JButton("Time Entry Screen");
        JButton backButton = new JButton("Back to Admin Menu");
    
        buttonsPanel.add(editButton);
        buttonsPanel.add(createButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(payTypeScreenButton);
        buttonsPanel.add(backButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
    
        // Edit Button Action
        editButton.addActionListener(e -> editSelectedUser());
    
        // Create Button Action
        createButton.addActionListener(e -> openUserDetails(""));
    
        // Delete Button Action
        deleteButton.addActionListener(e -> deleteSelectedUser());
    
        // Pay Type Screen Button Action
        payTypeScreenButton.addActionListener(e -> {
            int index = userList.getSelectedIndex();
            if (index >= 0) {
                String userID = userIDs.get(index);
                openPayTypeScreen(userID);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a user to open the Time Entry Screen.");
            }
        });
    
        // Back Button Action
        backButton.addActionListener(e -> {
            frame.dispose();
            new AdminMenu().displayMenu();
        });
    }
    
    private void loadUsers(String filter) {
        userListModel.clear();
        userIDs = new java.util.ArrayList<>();
    
        String query = "SELECT UserID, FirstName, LastName, Department, JobTitle, Email FROM Users " +
                "WHERE UserID LIKE ? OR FirstName LIKE ? OR LastName LIKE ? OR Department LIKE ? OR JobTitle LIKE ? OR Email LIKE ?";
    
        try (Connection connection = DatabaseHandler.connect();
             PreparedStatement stmt = connection.prepareStatement(query)) {
    
            String searchPattern = "%" + filter + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setString(5, searchPattern);
            stmt.setString(6, searchPattern);
    
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String userID = rs.getString("UserID");
                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                String department = rs.getString("Department") != null ? rs.getString("Department") : "N/A";
                String jobTitle = rs.getString("JobTitle") != null ? rs.getString("JobTitle") : "N/A";
                String email = rs.getString("Email");
    
                userListModel.addElement(userID + ": " + name + ", " + department + ", " + jobTitle + ", " + email);
                userIDs.add(userID);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading users: " + e.getMessage());
        }
    }
    
    private void editSelectedUser() {
        int index = userList.getSelectedIndex();
        if (index >= 0) {
            String userID = userIDs.get(index);
            openUserDetails(userID);
        } else {
            JOptionPane.showMessageDialog(null, "Please select a user to edit.");
        }
    }
    
    private void deleteSelectedUser() {
        int index = userList.getSelectedIndex();
        if (index >= 0) {
            String userID = userIDs.get(index);
            int confirmation = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete User ID: " + userID + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                try (Connection connection = DatabaseHandler.connect();
                     PreparedStatement stmt = connection.prepareStatement("DELETE FROM Users WHERE UserID = ?")) {
                    stmt.setString(1, userID);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "User deleted successfully!");
                    loadUsers(""); // Refresh the list
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error deleting user: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a user to delete.");
        }
    }
    
    private void openUserDetails(String userID) {
        JFrame detailFrame = new JFrame(userID.isEmpty() ? "Add New User" : "Edit User Details");
        detailFrame.setSize(800, 800);
        detailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        detailFrame.add(mainPanel);
    
        UserDetailsForm userForm = new UserDetailsForm(mainPanel, userID);
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            if (userForm.saveDetails(userID)) {
                detailFrame.dispose();
                loadUsers("");
            }
        });
        buttonPanel.add(saveButton);
    
        JButton payrollButton = new JButton("Edit Payroll");
        payrollButton.addActionListener(e -> {
            JFrame payrollFrame = new JFrame("Edit Payroll Information");
            payrollFrame.setSize(600, 400);
    
            JPanel payrollPanel = new JPanel();
            payrollFrame.add(payrollPanel);
    
            UserPayrollDetailsForm userPayrollForm = new UserPayrollDetailsForm(payrollPanel, userID);
    
            payrollFrame.setVisible(true);
        });
        buttonPanel.add(payrollButton);
    
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> detailFrame.dispose());
        buttonPanel.add(closeButton);
    
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);
    
        detailFrame.setVisible(true);
    }
    
    
    
class UserDetailsForm {
        private final JTextField[] fields = new JTextField[12];
        private final JComboBox<String> roleComboBox;
        private final JComboBox<String> statusComboBox;
        private final JComboBox<String> genderComboBox;
        private final JPasswordField passwordField;
        private final JLabel pictureLabel = new JLabel(); // Initialize directly
        private byte[] pictureData; // Picture data
    
        private final List<String> roles = Arrays.asList("Admin", "Employee");
        private final List<String> statuses = Arrays.asList("Active", "Terminated");
        private final List<String> genders = Arrays.asList("Male", "Female");
    
        private void createFieldRow(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel(label), gbc);
    
            gbc.gridx = 1;
            panel.add(field, gbc);
        }
    
        private void addPictureField(JPanel panel, GridBagConstraints gbc, int row) {
            pictureLabel.setPreferredSize(new Dimension(100, 100));
            pictureLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            pictureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
            JButton uploadButton = new JButton("Upload Picture");
            uploadButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = fileChooser.getSelectedFile();
                        pictureData = Files.readAllBytes(file.toPath());
                        ImageIcon icon = new ImageIcon(pictureData);
                        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        pictureLabel.setIcon(new ImageIcon(img));
                        JOptionPane.showMessageDialog(null, "Picture uploaded successfully.");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error uploading picture: " + ex.getMessage());
                    }
                }
            });
        
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel("Picture:"), gbc);
        
            gbc.gridx = 1;
            panel.add(pictureLabel, gbc);
        
            gbc.gridx = 2;
            panel.add(uploadButton, gbc);
        }
        
        UserDetailsForm(JPanel panel, String userID) {
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
    
            // Role
            roleComboBox = new JComboBox<>(roles.toArray(new String[0]));
            createFieldRow(panel, "Role:", roleComboBox, gbc, 0);
    
            // Status
            statusComboBox = new JComboBox<>(statuses.toArray(new String[0]));
            createFieldRow(panel, "Status:", statusComboBox, gbc, 1);
    
            // Gender
            genderComboBox = new JComboBox<>(genders.toArray(new String[0]));
            createFieldRow(panel, "Gender:", genderComboBox, gbc, 2);
    
            // User Detail Fields
            String[] labels = {"First Name:", "Last Name:", "Surname:", "Department:", "Job Title:", "Date of Birth:",
                               "Email:", "Address Line 1:", "Address Line 2:", "City:", "State:", "Zip Code:"};
            for (int i = 0; i < labels.length; i++) {
                fields[i] = new JTextField(20);
                createFieldRow(panel, labels[i], fields[i], gbc, i + 3);
            }
    
            // Password
            passwordField = new JPasswordField(20);
            createFieldRow(panel, "Override Password:", passwordField, gbc, labels.length + 3);
    
            // Picture
            addPictureField(panel, gbc, labels.length + 4);
    
            // Load user details if editing
            if (userID != null && !userID.isEmpty()) {
                loadDetails(userID);
            }
        }
    
        private void loadDetails(String userID) {
            String query = "SELECT Role, FirstName, LastName, SurName, Department, JobTitle, Status, DateOfBirth, Gender, Email, AddressLine1, AddressLine2, City, State, Zip, Picture " +
                           "FROM Users WHERE UserID = ?";
            try (Connection connection = DatabaseHandler.connect();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, userID);
                ResultSet rs = stmt.executeQuery();
    
                if (rs.next()) {
                    roleComboBox.setSelectedItem(rs.getString("Role"));
                    statusComboBox.setSelectedItem(rs.getString("Status"));
                    genderComboBox.setSelectedItem(rs.getString("Gender"));
    
                    fields[0].setText(rs.getString("FirstName"));
                    fields[1].setText(rs.getString("LastName"));
                    fields[2].setText(rs.getString("SurName"));
                    fields[3].setText(rs.getString("Department"));
                    fields[4].setText(rs.getString("JobTitle"));
                    fields[5].setText(rs.getString("DateOfBirth"));
                    fields[6].setText(rs.getString("Email"));
                    fields[7].setText(rs.getString("AddressLine1"));
                    fields[8].setText(rs.getString("AddressLine2"));
                    fields[9].setText(rs.getString("City"));
                    fields[10].setText(rs.getString("State"));
                    fields[11].setText(rs.getString("Zip"));
    
                    pictureData = rs.getBytes("Picture");
                    if (pictureData != null) {
                        ImageIcon icon = new ImageIcon(pictureData);
                        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        pictureLabel.setIcon(new ImageIcon(img));
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Error: User not found.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading user details: " + e.getMessage());
            }
        }
    
        private boolean saveDetails(String userID) {
            String query;
            if (userID == null || userID.isEmpty()) { // New user
                String role = roleComboBox.getSelectedItem().toString();
                userID = generateUniqueUserID(role);
                query = "INSERT INTO Users (UserID, Role, FirstName, LastName, SurName, Department, JobTitle, Status, DateOfBirth, Gender, Email, AddressLine1, AddressLine2, City, State, Zip, Password, Picture) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            } else { // Update existing user
                query = "UPDATE Users SET Role = ?, FirstName = ?, LastName = ?, SurName = ?, Department = ?, JobTitle = ?, Status = ?, DateOfBirth = ?, Gender = ?, Email = ?, AddressLine1 = ?, AddressLine2 = ?, City = ?, State = ?, Zip = ?, Password = ?, Picture = ? WHERE UserID = ?";
            }
        
            try (Connection connection = DatabaseHandler.connect();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                int paramIndex = 1;
        
                if (userID != null && !userID.isEmpty()) {
                    stmt.setString(paramIndex++, userID);
                }
        
                stmt.setString(paramIndex++, roleComboBox.getSelectedItem().toString());
                stmt.setString(paramIndex++, fields[0].getText().trim());
                stmt.setString(paramIndex++, fields[1].getText().trim());
                stmt.setString(paramIndex++, fields[2].getText().trim().isEmpty() ? null : fields[2].getText().trim());
                stmt.setString(paramIndex++, fields[3].getText().trim().isEmpty() ? null : fields[3].getText().trim());
                stmt.setString(paramIndex++, fields[4].getText().trim());
                stmt.setString(paramIndex++, statusComboBox.getSelectedItem().toString());
                stmt.setString(paramIndex++, fields[5].getText().trim().isEmpty() ? null : fields[5].getText().trim());
                stmt.setString(paramIndex++, genderComboBox.getSelectedItem().toString());
                stmt.setString(paramIndex++, fields[6].getText().trim());
                stmt.setString(paramIndex++, fields[7].getText().trim());
                stmt.setString(paramIndex++, fields[8].getText().trim().isEmpty() ? null : fields[8].getText().trim());
                stmt.setString(paramIndex++, fields[9].getText().trim());
                stmt.setString(paramIndex++, fields[10].getText().trim());
                stmt.setString(paramIndex++, fields[11].getText().trim().isEmpty() ? null : fields[11].getText().trim());
        
                String password = new String(passwordField.getPassword()).trim();
                if (password.isEmpty()) {
                    password = (userID != null && !userID.isEmpty()) ? fetchCurrentPassword(userID) : Security.generateDefaultPassword(fields[6].getText().trim(), fields[5].getText().trim());
                } else {
                    password = Security.hashPassword(password);
                }
                stmt.setString(paramIndex++, password);
        
                stmt.setBytes(paramIndex++, pictureData);
        
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "User details saved successfully!");
                return true;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error saving user details: " + e.getMessage());
                return false;
            }
        }
        
    
        private String fetchCurrentPassword(String userID) {
            String query = "SELECT Password FROM Users WHERE UserID = ?";
            try (Connection connection = DatabaseHandler.connect();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, userID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("Password");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error fetching current password: " + e.getMessage());
            }
            return Security.hashPassword("Default123"); // Fallback password
        }

        private String generateUniqueUserID(String role) {
            String prefix = role.equalsIgnoreCase("Admin") ? "HR" : "EM";
            String query = "SELECT UserID FROM Users WHERE UserID LIKE ? ORDER BY UserID DESC LIMIT 1";
            String newID = "";
        
            try (Connection connection = DatabaseHandler.connect();
                 PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, prefix + "%");
                ResultSet rs = stmt.executeQuery();
        
                if (rs.next()) {
                    String lastID = rs.getString("UserID");
                    int numberPart = Integer.parseInt(lastID.substring(2)); // Extract numeric part
                    newID = prefix + String.format("%04d", numberPart + 1); // Increment and format to 4 digits
                } else {
                    newID = prefix + "0001"; // Start from 0001 if no IDs exist
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error generating User ID: " + e.getMessage());
            }
        
            return newID;
        }
        
    }
    
    public static void openPayTypeScreen(String userID) {
        try (Connection connection = DatabaseHandler.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT PayType FROM Payroll WHERE UserID = ?")) {
            stmt.setString(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                new EmployeeTimeEntry().displayTimeEntryScreen(userID);
            } else {
                JOptionPane.showMessageDialog(null, "No Payroll data found for User ID: " + userID);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error retrieving pay type: " + e.getMessage());
        }
    }
    
    
    class UserPayrollDetailsForm {

        private void createFieldRow(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel(label), gbc);
    
            gbc.gridx = 1;
            panel.add(field, gbc);
        }
    
        UserPayrollDetailsForm(JPanel panel, String userID) {
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
    
            String[] labels = {"Pay Type:", "Hourly Rate:", "Federal Tax Rate:", "State Tax Rate:",
                               "Social Security Rate:", "Medicare Rate:", "Other Deductions:",
                               "Dependents:", "Medical Coverage:", "PTO Hours:"};
            JComponent[] fields = new JComponent[labels.length];
    
            // Create form fields dynamically
            for (int i = 0; i < labels.length; i++) {
                if (i == 0) { // Pay Type
                    fields[i] = new JComboBox<>(new String[]{"Salary", "Hourly"});
                } else if (i == 8) { // Medical Coverage
                    fields[i] = new JComboBox<>(new String[]{"Single", "Family"});
                } else if (i == 7) { // Dependents
                    fields[i] = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1)); // Range: 0-10 dependents
                } else {
                    fields[i] = new JTextField(20); // PTO Hours included as JTextField
                }
                createFieldRow(panel, labels[i], fields[i], gbc, i);
            }
    
            // Buttons
            JPanel buttonPanel = new JPanel();
            JButton saveButton = new JButton("Save Payroll Information");
            saveButton.addActionListener(e -> {
                if (validateFields(fields)) {
                    savePayrollDetails(userID, fields);
                }
            });
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> SwingUtilities.getWindowAncestor(panel).dispose());
            buttonPanel.add(saveButton);
            buttonPanel.add(closeButton);
    
            gbc.gridx = 0;
            gbc.gridy = labels.length;
            gbc.gridwidth = 2;
            panel.add(buttonPanel, gbc);
    
            // Load existing payroll details or defaults
            if (userID != null && !userID.isEmpty()) {
                loadPayrollDetails(userID, fields);
            } else {
                loadDefaultValues(fields);
            }
        }
    
        private void loadPayrollDetails(String userID, JComponent[] fields) {
            try (Connection connection = DatabaseHandler.connect();
                 PreparedStatement stmt = connection.prepareStatement(
                         "SELECT PayType, HourlyRate, FederalTax, StateTax, SocialSecurity, Medicare, " +
                         "OtherDeductions, Dependents, MedicalCoverage, PTO FROM Payroll WHERE UserID = ?")) {
                stmt.setString(1, userID);
                ResultSet rs = stmt.executeQuery();
    
                if (rs.next()) {
                    ((JComboBox<String>) fields[0]).setSelectedItem(rs.getString("PayType"));
                    ((JTextField) fields[1]).setText(String.valueOf(rs.getDouble("HourlyRate")));
                    ((JTextField) fields[2]).setText(String.valueOf(rs.getDouble("FederalTax")));
                    ((JTextField) fields[3]).setText(String.valueOf(rs.getDouble("StateTax")));
                    ((JTextField) fields[4]).setText(String.valueOf(rs.getDouble("SocialSecurity")));
                    ((JTextField) fields[5]).setText(String.valueOf(rs.getDouble("Medicare")));
                    ((JTextField) fields[6]).setText(String.valueOf(rs.getDouble("OtherDeductions")));
                    ((JSpinner) fields[7]).setValue(rs.getInt("Dependents"));
                    ((JComboBox<String>) fields[8]).setSelectedItem(rs.getString("MedicalCoverage"));
                    ((JTextField) fields[9]).setText(String.valueOf(rs.getDouble("PTO"))); // Load PTO Hours
                } else {
                    JOptionPane.showMessageDialog(null, "No payroll details found. Loading default values.");
                    loadDefaultValues(fields);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error loading payroll details: " + e.getMessage());
            }
        }
    
        private void loadDefaultValues(JComponent[] fields) {
            ((JTextField) fields[1]).setText("0.0");
            ((JTextField) fields[2]).setText("0.0765");
            ((JTextField) fields[3]).setText("0.0315");
            ((JTextField) fields[4]).setText("0.062");
            ((JTextField) fields[5]).setText("0.0145");
            ((JTextField) fields[6]).setText("0.0");
            ((JSpinner) fields[7]).setValue(0);
            ((JComboBox<String>) fields[0]).setSelectedItem("Hourly");
            ((JComboBox<String>) fields[8]).setSelectedItem("Single");
            ((JTextField) fields[9]).setText("0.0"); // Default PTO Hours
        }
    
        private boolean validateFields(JComponent[] fields) {
            String[] fieldNames = {"Pay Type", "Hourly Rate", "Federal Tax Rate", "State Tax Rate",
                                   "Social Security Rate", "Medicare Rate", "Other Deductions",
                                   "Dependents", "Medical Coverage", "PTO Hours"};
    
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] instanceof JTextField) {
                    String text = ((JTextField) fields[i]).getText().trim();
                    if (text.isEmpty() || !isNumeric(text)) {
                        JOptionPane.showMessageDialog(null, fieldNames[i] + " must be a valid number.");
                        return false;
                    }
                    if (i == 9) { // PTO Hours Validation
                        double ptoValue = Double.parseDouble(text);
                        if (ptoValue < 0) {
                            JOptionPane.showMessageDialog(null, fieldNames[i] + " cannot be negative.");
                            return false;
                        }
                    }
                } else if (fields[i] instanceof JSpinner) {
                    if ((int) ((JSpinner) fields[i]).getValue() < 0) {
                        JOptionPane.showMessageDialog(null, fieldNames[i] + " must be a non-negative number.");
                        return false;
                    }
                }
            }
            return true;
        }
    
        private void savePayrollDetails(String userID, JComponent[] fields) {
            try (Connection connection = DatabaseHandler.connect()) {
                boolean recordExists;
    
                // Check if a payroll record exists for the userID
                try (PreparedStatement checkStmt = connection.prepareStatement(
                        "SELECT COUNT(*) FROM Payroll WHERE UserID = ?")) {
                    checkStmt.setString(1, userID);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    recordExists = rs.getInt(1) > 0;
                }
    
                if (recordExists) {
                    // Update existing record
                    try (PreparedStatement updateStmt = connection.prepareStatement(
                            "UPDATE Payroll SET PayType = ?, HourlyRate = ?, FederalTax = ?, StateTax = ?, " +
                            "SocialSecurity = ?, Medicare = ?, OtherDeductions = ?, Dependents = ?, " +
                            "MedicalCoverage = ?, PTO = ? WHERE UserID = ?")) {
    
                        updatePayrollDetails(updateStmt, userID, fields);
                        JOptionPane.showMessageDialog(null, "Payroll information updated successfully.");
                    }
                } else {
                    // Insert new record
                    try (PreparedStatement insertStmt = connection.prepareStatement(
                            "INSERT INTO Payroll (UserID, PayType, HourlyRate, FederalTax, StateTax, " +
                            "SocialSecurity, Medicare, OtherDeductions, Dependents, MedicalCoverage, PTO) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
    
                        insertPayrollDetails(insertStmt, userID, fields);
                        JOptionPane.showMessageDialog(null, "New payroll record created successfully.");
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error saving payroll details: " + e.getMessage());
            }
        }
    
        private void updatePayrollDetails(PreparedStatement stmt, String userID, JComponent[] fields) throws SQLException {
            stmt.setString(1, ((JComboBox<String>) fields[0]).getSelectedItem().toString());
            stmt.setDouble(2, Double.parseDouble(((JTextField) fields[1]).getText()));
            stmt.setDouble(3, Double.parseDouble(((JTextField) fields[2]).getText()));
            stmt.setDouble(4, Double.parseDouble(((JTextField) fields[3]).getText()));
            stmt.setDouble(5, Double.parseDouble(((JTextField) fields[4]).getText()));
            stmt.setDouble(6, Double.parseDouble(((JTextField) fields[5]).getText()));
            stmt.setDouble(7, Double.parseDouble(((JTextField) fields[6]).getText()));
            stmt.setInt(8, (int) ((JSpinner) fields[7]).getValue());
            stmt.setString(9, ((JComboBox<String>) fields[8]).getSelectedItem().toString());
            stmt.setDouble(10, Double.parseDouble(((JTextField) fields[9]).getText())); // Save PTO Hours
            stmt.setString(11, userID);
            stmt.executeUpdate();
        }
    
        private void insertPayrollDetails(PreparedStatement stmt, String userID, JComponent[] fields) throws SQLException {
            stmt.setString(1, userID);
            stmt.setString(2, ((JComboBox<String>) fields[0]).getSelectedItem().toString());
            stmt.setDouble(3, Double.parseDouble(((JTextField) fields[1]).getText()));
            stmt.setDouble(4, Double.parseDouble(((JTextField) fields[2]).getText()));
            stmt.setDouble(5, Double.parseDouble(((JTextField) fields[3]).getText()));
            stmt.setDouble(6, Double.parseDouble(((JTextField) fields[4]).getText()));
            stmt.setDouble(7, Double.parseDouble(((JTextField) fields[5]).getText()));
            stmt.setDouble(8, Double.parseDouble(((JTextField) fields[6]).getText()));
            stmt.setInt(9, (int) ((JSpinner) fields[7]).getValue());
            stmt.setString(10, ((JComboBox<String>) fields[8]).getSelectedItem().toString());
            stmt.setDouble(11, Double.parseDouble(((JTextField) fields[9]).getText())); // Save PTO Hours
            stmt.executeUpdate();
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
}    