package main.Admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.ZoneId;
import main.Database.DatabaseHandler;

class PayrollManagement {
    public void displayPayrollManagement() {
        JFrame timeEntryFrame = new JFrame("Employee Time Entry Screen");
        timeEntryFrame.setSize(1000, 600);
        timeEntryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel searchPanel = new JPanel(new FlowLayout());
        JTextField userIdField = new JTextField(10);
        JSpinner payPeriodSpinner = createPayPeriodSpinner();
        JButton searchButton = new JButton("Search");
        JButton calculateButton = new JButton("Calculate Payroll");
        JButton viewPayslipButton = new JButton("View Payslip"); // New Button
        JButton createReportButton = new JButton("Create Report");

        searchPanel.add(new JLabel("User ID:"));
        searchPanel.add(userIdField);
        searchPanel.add(new JLabel("Pay Period Start:"));
        searchPanel.add(payPeriodSpinner);
        searchPanel.add(searchButton);
        searchPanel.add(calculateButton);
        searchPanel.add(viewPayslipButton); // Add to Panel
        searchPanel.add(createReportButton);

        JTable timeEntryTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(timeEntryTable);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        timeEntryFrame.add(mainPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton backButton = new JButton("Back");
        buttonsPanel.add(backButton);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> {
            timeEntryFrame.dispose();
            new AdminMenu().displayMenu();
        });

        searchButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String payPeriodStart = ((JSpinner.DateEditor) payPeriodSpinner.getEditor())
                    .getFormat().format(payPeriodSpinner.getValue());
            updateTimeEntryTable(timeEntryTable, userId, payPeriodStart);
        });

        calculateButton.addActionListener(e -> {
            String payPeriodStart = ((JSpinner.DateEditor) payPeriodSpinner.getEditor())
                    .getFormat().format(payPeriodSpinner.getValue());
            calculateTaxesForPayroll(payPeriodStart);
        });

        viewPayslipButton.addActionListener(e -> {
            int selectedRow = timeEntryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a time entry to view the payslip.");
                return;
            }

            String timeEntryID = timeEntryTable.getValueAt(selectedRow, 0).toString();
            showPayslip(timeEntryID);
        });

        createReportButton.addActionListener(e -> {
            String payPeriodStart = ((JSpinner.DateEditor) payPeriodSpinner.getEditor())
                    .getFormat().format(payPeriodSpinner.getValue());
            createPayrollReport(payPeriodStart);
        });

        timeEntryFrame.setVisible(true);

        updateTimeEntryTable(timeEntryTable, "", "");
    }

    private void updateTimeEntryTable(JTable table, String userId, String payPeriodStart) {
        try (Connection connection = DatabaseHandler.connect()) {
            String query = "SELECT EntryID, UserID, " +
                           "strftime('%Y-%m-%d', PayPeriodStart / 1000, 'unixepoch') AS PayPeriodStart, " +
                           "RegularHours, OvertimeHours, PTOHours, Locked " +
                           "FROM TimeEntries WHERE 1=1";

            if (!userId.isEmpty()) {
                query += " AND UserID = ?";
            }
            if (!payPeriodStart.isEmpty()) {
                query += " AND strftime('%Y-%m-%d', PayPeriodStart / 1000, 'unixepoch') = ?";
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                int paramIndex = 1;
                if (!userId.isEmpty()) {
                    stmt.setString(paramIndex++, userId);
                }
                if (!payPeriodStart.isEmpty()) {
                    stmt.setString(paramIndex, payPeriodStart);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    String[] columnNames = {"Entry ID", "User ID", "Pay Period Start", "Regular Hours", "Overtime Hours", "PTO Hours", "Locked"};
                    table.setModel(buildTableModel(rs, columnNames));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error fetching time entry data: " + e.getMessage());
        }
    }

    private DefaultTableModel buildTableModel(ResultSet rs, String[] columnNames) throws Exception {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);

        while (rs.next()) {
            Object[] row = new Object[columnNames.length];
            for (int i = 0; i < columnNames.length; i++) {
                row[i] = rs.getObject(i + 1);
            }
            model.addRow(row);
        }
        return model;
    }

    private JSpinner createPayPeriodSpinner() {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(dateModel);

        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);

        spinner.setValue(java.util.Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        return spinner;
    }

    private void calculateTaxesForPayroll(String payPeriodStart) {
        try (Connection connection = DatabaseHandler.connect()) {
            String query = "SELECT SUM(GrossPay) AS TotalGross, " +
                           "SUM(FederalTax) AS TotalFederalTax, " +
                           "SUM(StateTax) AS TotalStateTax, " +
                           "SUM(SocialSecurity) AS TotalSocialSecurity, " +
                           "SUM(Medicare) AS TotalMedicare, " +
                           "SUM(NetPay) AS TotalNet " +
                           "FROM PaySlip WHERE strftime('%Y-%m-%d', PayPeriodStart / 1000, 'unixepoch') = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, payPeriodStart);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal totalGross = rs.getBigDecimal("TotalGross");
                        BigDecimal totalFederalTax = rs.getBigDecimal("TotalFederalTax");
                        BigDecimal totalStateTax = rs.getBigDecimal("TotalStateTax");
                        BigDecimal totalSocialSecurity = rs.getBigDecimal("TotalSocialSecurity");
                        BigDecimal totalMedicare = rs.getBigDecimal("TotalMedicare");
                        BigDecimal totalNet = rs.getBigDecimal("TotalNet");

                        JOptionPane.showMessageDialog(null, String.format(
                                "Payroll Tax Summary for %s:\n\n" +
                                "Total Gross Pay: $%.2f\n" +
                                "Federal Tax: $%.2f\n" +
                                "State Tax: $%.2f\n" +
                                "Social Security Tax: $%.2f\n" +
                                "Medicare Tax: $%.2f\n" +
                                "Net Payroll: $%.2f",
                                payPeriodStart, totalGross, totalFederalTax, totalStateTax, totalSocialSecurity, totalMedicare, totalNet
                        ));
                    } else {
                        JOptionPane.showMessageDialog(null, "No payroll data found for the selected pay period: " + payPeriodStart);
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error calculating payroll taxes: " + e.getMessage());
        }
    }

    private void createPayrollReport(String payPeriodStart) {
        try (Connection connection = DatabaseHandler.connect()) {
            String query = "SELECT * FROM PaySlip WHERE strftime('%Y-%m-%d', PayPeriodStart / 1000, 'unixepoch') = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, payPeriodStart);

                try (ResultSet rs = stmt.executeQuery()) {
                    File reportFile = new File("Payroll_Report_" + payPeriodStart + ".txt");
                    try (FileWriter writer = new FileWriter(reportFile)) {
                        writer.write("Payroll Report for Pay Period Starting: " + payPeriodStart + "\n\n");
                        writer.write("PaySlipID\tUserID\tGrossPay\tFederalTax\tStateTax\tSocialSecurity\tMedicare\tNetPay\n");

                        while (rs.next()) {
                            writer.write(String.format("%d\t%s\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\n",
                                    rs.getInt("PaySlipID"),
                                    rs.getString("UserID"),
                                    rs.getDouble("GrossPay"),
                                    rs.getDouble("FederalTax"),
                                    rs.getDouble("StateTax"),
                                    rs.getDouble("SocialSecurity"),
                                    rs.getDouble("Medicare"),
                                    rs.getDouble("NetPay")));
                        }

                        JOptionPane.showMessageDialog(null, "Payroll report created: " + reportFile.getAbsolutePath());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Error writing payroll report: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error creating payroll report: " + e.getMessage());
        }
    }

    private void showPayslip(String timeEntryID) {
        try (Connection connection = DatabaseHandler.connect()) {
            String query = "SELECT * FROM PaySlip WHERE TimeEntryID = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, timeEntryID);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        JFrame payslipFrame = new JFrame("Payslip Details");
                        payslipFrame.setSize(400, 400);
                        payslipFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
                        panel.add(new JLabel("PaySlip ID:"));
                        panel.add(new JLabel(rs.getString("PaySlipID")));
                        panel.add(new JLabel("User ID:"));
                        panel.add(new JLabel(rs.getString("UserID")));
                        panel.add(new JLabel("Gross Pay:"));
                        panel.add(new JLabel(String.valueOf(rs.getDouble("GrossPay"))));
                        panel.add(new JLabel("Federal Tax:"));
                        panel.add(new JLabel(String.valueOf(rs.getDouble("FederalTax"))));
                        panel.add(new JLabel("State Tax:"));
                        panel.add(new JLabel(String.valueOf(rs.getDouble("StateTax"))));
                        panel.add(new JLabel("Net Pay:"));
                        panel.add(new JLabel(String.valueOf(rs.getDouble("NetPay"))));

                        payslipFrame.add(panel);
                        payslipFrame.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "No payslip found for the selected time entry.");
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error fetching payslip data: " + e.getMessage());
        }
    }
}
