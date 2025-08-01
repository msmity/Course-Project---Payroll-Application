package main.Login;

import javax.swing.*;

public class Login {

    public void displayLogin() {
        JFrame frame = new JFrame("Payroll System - Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel, frame);

        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel, JFrame frame) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("User ID:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(10, 80, 80, 25);
        panel.add(roleLabel);

        String[] roles = {"Admin", "Employee"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        roleComboBox.setBounds(100, 80, 165, 25);
        panel.add(roleComboBox);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 110, 150, 25);
        panel.add(loginButton);

        JButton exitButton = new JButton("Exit Program");
        exitButton.setBounds(10, 140, 150, 25);
        panel.add(exitButton);

        loginButton.addActionListener(e -> handleLogin(userText, passwordText, roleComboBox, frame));

        exitButton.addActionListener(e -> {
            frame.dispose();
            System.exit(0);
        });
    }

    private void handleLogin(JTextField userText, JPasswordField passwordText, JComboBox<String> roleComboBox, JFrame frame) {
        String userId = userText.getText().trim();
        String password = new String(passwordText.getPassword()).trim();
        String selectedRole = (String) roleComboBox.getSelectedItem();
        String storedRole = Security.validateRole(userId);

        if (!validateInputs(userId, password)) {
            JOptionPane.showMessageDialog(null, "User ID and Password cannot be empty or invalid.");
            return;
        }

        if (Security.validateLogin(userId, password)) {
            frame.dispose();
            UserSession.setUser(userId, storedRole);
            if (selectedRole == "Employee") {
                navigateToMenu(selectedRole);
            } else if (selectedRole != storedRole) {
                JOptionPane.showMessageDialog(null, "Access Denied: You do not have admin privileges.");
                navigateToMenu(storedRole);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid credentials. Please try again.");
        }
    }

    private boolean validateInputs(String userId, String password) {
        return userId != null && !userId.isEmpty() && userId.matches("[a-zA-Z0-9]+") &&
               password != null && !password.isEmpty();
    }    

    private void navigateToMenu(String role) {
        if ("Admin".equalsIgnoreCase(role)) {
            new main.Admin.AdminMenu().displayMenu();
        } else {
            new main.Employee.EmployeeMenu().displayMenu();
        }
    }
}
