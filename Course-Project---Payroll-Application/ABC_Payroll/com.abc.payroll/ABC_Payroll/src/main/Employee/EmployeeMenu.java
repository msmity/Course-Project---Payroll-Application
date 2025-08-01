package main.Employee;

import javax.swing.*;

import main.Admin.EmployeeManagement;
import main.Login.Login;
import main.Login.UserSession;

public class EmployeeMenu {
    public void displayMenu() {
        JFrame frame = new JFrame("Employee Menu");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel, frame);

        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel, JFrame frame) {
        panel.setLayout(null);

        JLabel welcomeLabel = new JLabel("Welcome, Employee: " + UserSession.getUserId());
        welcomeLabel.setBounds(10, 20, 300, 25);
        panel.add(welcomeLabel);

        JButton timeEntryButton = new JButton("Time Entry");
        timeEntryButton.setBounds(10, 50, 200, 25);
        panel.add(timeEntryButton);

        JButton backButton = new JButton("Logout");
        backButton.setBounds(10, 110, 200, 25);
        panel.add(backButton);



        timeEntryButton.addActionListener(e -> {
        String userID = UserSession.getUserId();
        EmployeeManagement.openPayTypeScreen(userID);
        });

        backButton.addActionListener(e -> {
            frame.dispose();
            JOptionPane.showMessageDialog(null, "You have logged out.");
            new Login().displayLogin();
        });
    }
}
