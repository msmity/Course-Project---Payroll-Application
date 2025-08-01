package main.Admin;

import javax.swing.*;
import main.Login.Login;
import main.Login.UserSession;

public class AdminMenu {

    public void displayMenu() {
        // Check if the session is valid before displaying the menu
        if (UserSession.isSessionExpired()) {
            JOptionPane.showMessageDialog(null, "Session expired. Please log in again.");
            new Login().displayLogin();
            return;
        }

        // Check if the current user is an admin
        if (!"Admin".equalsIgnoreCase(UserSession.getRole())) {
            JOptionPane.showMessageDialog(null, "Access denied. Admins only.");
            new Login().displayLogin();
            return;
        }

        JFrame frame = new JFrame("Admin Menu");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        setupMenuComponents(panel, frame);

        frame.setVisible(true);
    }

    private void setupMenuComponents(JPanel panel, JFrame frame) {
        panel.setLayout(null);

        JLabel welcomeLabel = new JLabel("Welcome, Admin: " + UserSession.getUserId());
        welcomeLabel.setBounds(10, 20, 300, 25);
        panel.add(welcomeLabel);

        JButton manageEmployeesButton = new JButton("Manage Employees");
        manageEmployeesButton.setBounds(10, 50, 200, 25);
        panel.add(manageEmployeesButton);

        JButton payrollButton = new JButton("Payroll Management");
        payrollButton.setBounds(10, 80, 200, 25);
        panel.add(payrollButton);
        
        JButton appInfoButton = new JButton("Application Info");
        appInfoButton.setBounds(10, 140, 200, 25);
        panel.add(appInfoButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(10, 170, 200, 25);
        panel.add(logoutButton);

        // Button actions
        manageEmployeesButton.addActionListener(e -> openEmployeeManagement(frame));
        payrollButton.addActionListener(e -> openPayrollManagement(frame));
        appInfoButton.addActionListener(e -> viewApplicationInfo());
        logoutButton.addActionListener(e -> logout(frame));
    }

    private void openEmployeeManagement(JFrame frame) {
        try {
            frame.dispose();
            EmployeeManagement employeeManagement = new EmployeeManagement();
            employeeManagement.displayEmployeeManagement();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error opening Employee Management: " + ex.getMessage());
        }
    }

    private void openPayrollManagement(JFrame frame) {
        try {
            frame.dispose(); // Close the current frame
            PayrollManagement payrollManagement = new PayrollManagement(); // Instantiate the PayrollManagement class
            payrollManagement.displayPayrollManagement(); // Call the display method
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error opening Payroll Management: " + ex.getMessage());
        }
    }

    private void viewApplicationInfo() {
        try {
            String appInfo = "Version: 1.0\nAuthor: Matthew Smith";
            JOptionPane.showMessageDialog(null, appInfo);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error retrieving application info: " + ex.getMessage());
        }
    }

    private void logout(JFrame frame) {
        UserSession.logout();
        JOptionPane.showMessageDialog(null, "You have logged out.");
        frame.dispose();
        new Login().displayLogin();
    }
}
