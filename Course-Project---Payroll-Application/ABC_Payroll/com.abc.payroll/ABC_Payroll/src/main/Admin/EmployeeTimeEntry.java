package main.Admin;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.awt.*;
import javax.swing.*;
import main.Database.DatabaseHandler;
import javax.swing.*;
class EmployeeTimeEntry {

    // Class-level JLabel references for updating in updatePayLabels
    private JLabel grossPayLabel;
    private JLabel overtimePayLabel;
    private JLabel medicalDeductionLabel;
    private JLabel dependentStipendLabel;
    private JLabel stateTaxLabel;
    private JLabel federalTaxLabel;
    private JLabel socialSecurityLabel;
    private JLabel medicareLabel;
    private JLabel netPayLabel;
    private JLabel employerFederalTaxLabel;
    private JLabel employerSocialSecurityLabel;
    private JLabel employerMedicareLabel;


    public void displayTimeEntryScreen(String userID) {
        PayrollData payrollData = PayrollData.initialize();

        // Fetch payroll details from database
        if (!fetchPayrollDetails(userID, payrollData)) return;

        JFrame frame = createMainFrame();

        JPanel mainPanel = createMainPanel();
        JPanel leftPanel = createLeftPanel(payrollData);
        JPanel rightPanel = createRightPanel(payrollData);
        JPanel buttonPanel = createButtonPanel(userID, payrollData, rightPanel);

        GridBagConstraints gbc = createGridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        mainPanel.add(leftPanel, gbc);

        gbc.gridx = 1;
        mainPanel.add(rightPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private JFrame createMainFrame() {
        JFrame frame = new JFrame("Employee Time Entry");
        frame.setSize(1000, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return frame;
    }

    private JPanel createMainPanel() {
        return new JPanel(new GridBagLayout());
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }

    private JPanel createLeftPanel(PayrollData payrollData) {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();
    
        int leftRow = 0;
        // Employee-specific information
        createFieldRow(leftPanel, "EMPLOYEE:", new JLabel(""), gbc, leftRow++);
        createFieldRow(leftPanel, "Pay Type:", new JLabel(payrollData.isSalary ? "Salary" : "Hourly"), gbc, leftRow++);
        createFieldRow(leftPanel, "Hourly Rate:", new JLabel("$" + payrollData.hourlyRate), gbc, leftRow++);
        createFieldRow(leftPanel, "Federal Tax:", new JLabel(payrollData.federalTaxRate + "%"), gbc, leftRow++);
        createFieldRow(leftPanel, "State Tax:", new JLabel(payrollData.stateTaxRate + "%"), gbc, leftRow++);
        createFieldRow(leftPanel, "Social Security:", new JLabel(payrollData.socialSecurityRate + "%"), gbc, leftRow++);
        createFieldRow(leftPanel, "Medicare:", new JLabel(payrollData.medicareRate + "%"), gbc, leftRow++);
        createFieldRow(leftPanel, "Other Deductions:", new JLabel("$" + payrollData.getOtherDeductions()), gbc, leftRow++);
        createFieldRow(leftPanel, "Dependents:", new JLabel(String.valueOf(payrollData.dependents)), gbc, leftRow++);
        createFieldRow(leftPanel, "Medical Coverage:", new JLabel(payrollData.medicalCoverage), gbc, leftRow++);
    
        // Employer-specific information (hardcoded)
        createFieldRow(leftPanel, "EMPLOYER:", new JLabel(""), gbc, leftRow++);
        createFieldRow(leftPanel, "Federal Tax:", new JLabel("7.65%"), gbc, leftRow++);
        createFieldRow(leftPanel, "Social Security:", new JLabel("6.2%"), gbc, leftRow++);
        createFieldRow(leftPanel, "Medicare:", new JLabel("1.45%"), gbc, leftRow++);
    
        return leftPanel;
    }
    

    

    private JPanel createRightPanel(PayrollData payrollData) {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createGridBagConstraints();
        int rightRow = 0;
    
        // Labels for payroll details
        grossPayLabel = new JLabel("$0.00");
        overtimePayLabel = new JLabel("$0.00 +");
        medicalDeductionLabel = new JLabel("$0.00 -");
        dependentStipendLabel = new JLabel("$0.00 +");
        stateTaxLabel = new JLabel("$0.00 -");
        federalTaxLabel = new JLabel("$0.00 -");
        socialSecurityLabel = new JLabel("$0.00 -");
        medicareLabel = new JLabel("$0.00 -");
        netPayLabel = new JLabel("$0.00");
        employerFederalTaxLabel = new JLabel("$0.00");
        employerSocialSecurityLabel = new JLabel("$0.00");
        employerMedicareLabel = new JLabel("$0.00");
    
        // Add pay period start selector
        JLabel payPeriodLabel = new JLabel("Pay Period Start:");
        JSpinner payPeriodSpinner = createPayPeriodSpinner();
    
        createFieldRow(rightPanel, "EMPLOYEE:", new JLabel(), gbc, rightRow++);
        createFieldRow(rightPanel, "Gross Pay:", grossPayLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Overtime Pay:", overtimePayLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Medical Deduction:", medicalDeductionLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Dependent Stipend:", dependentStipendLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "State Tax:", stateTaxLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Federal Tax:", federalTaxLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Social Security:", socialSecurityLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Medicare:", medicareLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Net Pay:", netPayLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "EMPLOYER:", new JLabel(), gbc, rightRow++);
        createFieldRow(rightPanel, "Federal Tax:", employerFederalTaxLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Social Security:", employerSocialSecurityLabel, gbc, rightRow++);
        createFieldRow(rightPanel, "Medicare:", employerMedicareLabel, gbc, rightRow++);
        createFieldRow(rightPanel, payPeriodLabel.getText(), payPeriodSpinner, gbc, rightRow++);
    
        // Add days and worked hours fields
        JLabel[] dayLabels = new JLabel[7];
        JTextField[] workedHoursFields = new JTextField[7];
        JComboBox<String>[] ptoSelectors = new JComboBox[7];
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    
        for (int i = 0; i < 7; i++) {
            dayLabels[i] = new JLabel(daysOfWeek[i] + " Worked Hours:");
            workedHoursFields[i] = new JTextField();
            ptoSelectors[i] = new JComboBox<>(new String[] {"Regular Hours", "PTO"});
    
            if (payrollData.isSalary) {
                workedHoursFields[i].setText("8");
                workedHoursFields[i].setEditable(false);
            } else {
                workedHoursFields[i].setInputVerifier(new WorkedHoursInputVerifier());
            }
    
            createFieldRow(rightPanel, dayLabels[i].getText(), workedHoursFields[i], gbc, rightRow++);
            createFieldRow(rightPanel, "Type:", ptoSelectors[i], gbc, rightRow++);
        }
    
        // Store spinner and hours fields in the panel for later retrieval
        rightPanel.putClientProperty("payPeriodSpinner", payPeriodSpinner);
        rightPanel.putClientProperty("workedHoursFields", workedHoursFields);
    
        return rightPanel;
    }

private JPanel createButtonPanel(String userID, PayrollData payrollData, JPanel rightPanel) {
    JPanel buttonPanel = new JPanel();

    JButton calculateButton = new JButton("Calculate");
    calculateButton.addActionListener(e -> updatePayLabels(rightPanel, payrollData));

    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> updatePayLabels(rightPanel, payrollData));
    saveButton.addActionListener(e -> {
        // Extract the selected Pay Period Start date
        JSpinner payPeriodSpinner = (JSpinner) rightPanel.getClientProperty("payPeriodSpinner");
        LocalDate payPeriodStart = ((java.util.Date) payPeriodSpinner.getValue())
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

        // Extract worked hours fields
        JTextField[] workedHoursFields = (JTextField[]) rightPanel.getClientProperty("workedHoursFields");

        // Save time entries with the selected Pay Period Start date
        saveTimeEntries(userID, workedHoursFields, payPeriodStart, payrollData);
    });

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> SwingUtilities.getWindowAncestor(buttonPanel).dispose());

    buttonPanel.add(calculateButton);
    buttonPanel.add(saveButton);
    buttonPanel.add(closeButton);

    return buttonPanel;
}

private JSpinner createPayPeriodSpinner() {
    // Create a spinner with a date model
    SpinnerDateModel dateModel = new SpinnerDateModel();
    JSpinner spinner = new JSpinner(dateModel);

    // Format the date
    JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
    spinner.setEditor(editor);

    // Set default value to today's date
    spinner.setValue(java.util.Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

    return spinner;
}

    private void updatePayLabels(JPanel rightPanel, PayrollData payrollData) {
    JTextField[] workedHoursFields = extractWorkedHours(rightPanel);
    JComboBox<String>[] ptoSelectors = extractPTOSelectors(rightPanel);

    BigDecimal totalRegularHours = BigDecimal.ZERO;
    BigDecimal totalOvertimeHours = BigDecimal.ZERO;
    BigDecimal totalWorkedHours = BigDecimal.ZERO;
    BigDecimal totalPTOHours = BigDecimal.ZERO; 
    String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    for (int i = 0; i < workedHoursFields.length; i++) {
        String text = workedHoursFields[i].getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in the worked hours for " + daysOfWeek[i], "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal workedHours;
        try {
            workedHours = new BigDecimal(text);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid input for " + daysOfWeek[i] + ". Please enter a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (workedHours.compareTo(BigDecimal.ZERO) < 0) {
            JOptionPane.showMessageDialog(null, "Worked hours cannot be negative for " + daysOfWeek[i] + ".", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String ptoType = (String) ptoSelectors[i].getSelectedItem();
        if ("PTO".equals(ptoType)) {
            totalPTOHours = totalPTOHours.add(workedHours);
        }

        totalWorkedHours = totalWorkedHours.add(workedHours);
        if (totalWorkedHours.compareTo(new BigDecimal("80")) > 0) {
            JOptionPane.showMessageDialog(null, "Excessive hours worked: " + totalWorkedHours + " hours. Please review your inputs.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Saturday".equals(daysOfWeek[i])) {
            totalOvertimeHours = totalOvertimeHours.add(workedHours);
        } else {
            totalOvertimeHours = totalOvertimeHours.add(calculateOvertimeHours(workedHours, daysOfWeek[i]));
            totalRegularHours = totalRegularHours.add(workedHours.min(new BigDecimal("8")));
        }
    }

    if (totalPTOHours.compareTo(payrollData.getPTO()) > 0) {
        JOptionPane.showMessageDialog(null, "Insufficient PTO hours. You have " + payrollData.getPTO() + " PTO hours available.", "PTO Overdrawn", JOptionPane.ERROR_MESSAGE);
        return;
    }

    payrollData.adjustPTO(totalPTOHours);

    // Calculate gross pay before pre-tax deductions
    BigDecimal grossPay = totalRegularHours.multiply(payrollData.hourlyRate)
            .add(totalOvertimeHours.multiply(payrollData.hourlyRate.multiply(new BigDecimal("1.5"))));

    // Deduct medical coverage before calculating taxes
    BigDecimal preTaxPay = grossPay.subtract(payrollData.getMedicalDeduction());

    BigDecimal federalTax = preTaxPay.multiply(payrollData.federalTaxRate);
    BigDecimal stateTax = preTaxPay.multiply(payrollData.stateTaxRate);
    BigDecimal socialSecurity = preTaxPay.multiply(payrollData.socialSecurityRate);
    BigDecimal medicare = preTaxPay.multiply(payrollData.medicareRate);

    BigDecimal dependentStipend = payrollData.getDependentStipend();
    BigDecimal netPay = preTaxPay.subtract(federalTax).subtract(stateTax).subtract(socialSecurity).subtract(medicare)
            .add(dependentStipend);

    BigDecimal employerFederalTax = grossPay.multiply(new BigDecimal("0.0765"));
    BigDecimal employerSocialSecurity = grossPay.multiply(new BigDecimal("0.062"));
    BigDecimal employerMedicare = grossPay.multiply(new BigDecimal("0.0145"));

    grossPayLabel.setText("$" + grossPay.setScale(2, BigDecimal.ROUND_HALF_UP));
    overtimePayLabel.setText("$" + totalOvertimeHours.multiply(payrollData.hourlyRate.multiply(new BigDecimal("1.5"))).setScale(2, BigDecimal.ROUND_HALF_UP));
    medicalDeductionLabel.setText("$" + payrollData.getMedicalDeduction().setScale(2, BigDecimal.ROUND_HALF_UP));
    dependentStipendLabel.setText("$" + dependentStipend.setScale(2, BigDecimal.ROUND_HALF_UP));
    stateTaxLabel.setText("$" + stateTax.setScale(2, BigDecimal.ROUND_HALF_UP));
    federalTaxLabel.setText("$" + federalTax.setScale(2, BigDecimal.ROUND_HALF_UP));
    socialSecurityLabel.setText("$" + socialSecurity.setScale(2, BigDecimal.ROUND_HALF_UP));
    medicareLabel.setText("$" + medicare.setScale(2, BigDecimal.ROUND_HALF_UP));
    netPayLabel.setText("$" + netPay.setScale(2, BigDecimal.ROUND_HALF_UP));

    employerFederalTaxLabel.setText("$" + employerFederalTax.setScale(2, BigDecimal.ROUND_HALF_UP));
    employerSocialSecurityLabel.setText("$" + employerSocialSecurity.setScale(2, BigDecimal.ROUND_HALF_UP));
    employerMedicareLabel.setText("$" + employerMedicare.setScale(2, BigDecimal.ROUND_HALF_UP));
}

    
    private JComboBox<String>[] extractPTOSelectors(JPanel rightPanel) {
        JComboBox<String>[] ptoSelectors = new JComboBox[7];
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int index = 0;
    
        // Iterate through components of the rightPanel
        for (Component component : rightPanel.getComponents()) {
            if (component instanceof JComboBox && index < daysOfWeek.length) {
                ptoSelectors[index++] = (JComboBox<String>) component;
            }
        }
    
        // Ensure all fields are populated
        if (index < daysOfWeek.length) {
            throw new IllegalStateException("Insufficient JComboBoxes in rightPanel for PTO selectors.");
        }
    
        return ptoSelectors;
    }     
    
    

    private JTextField[] extractWorkedHours(JPanel rightPanel) {
        JTextField[] workedHoursFields = new JTextField[7];
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int index = 0;
    
        // Iterate through components of the rightPanel
        for (Component component : rightPanel.getComponents()) {
            if (component instanceof JTextField && index < daysOfWeek.length) {
                workedHoursFields[index++] = (JTextField) component;
            }
        }
    
        // Ensure all fields are populated
        if (index < daysOfWeek.length) {
            throw new IllegalStateException("Insufficient JTextFields in rightPanel for worked hours.");
        }
    
        return workedHoursFields;
    }
    

    private boolean fetchPayrollDetails(String userID, PayrollData payrollData) {
        try (Connection connection = DatabaseHandler.connect();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT PayType, HourlyRate, MedicalCoverage, Dependents, FederalTax, StateTax, SocialSecurity, Medicare, OtherDeductions, PTO FROM Payroll WHERE UserID = ?")) {
            stmt.setString(1, userID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                payrollData.isSalary = rs.getString("PayType").equals("Salary");
                payrollData.hourlyRate = new BigDecimal(rs.getString("HourlyRate"));
                payrollData.medicalCoverage = rs.getString("MedicalCoverage");
                payrollData.dependents = rs.getInt("Dependents");
                payrollData.federalTaxRate = new BigDecimal(rs.getString("FederalTax"));
                payrollData.stateTaxRate = new BigDecimal(rs.getString("StateTax"));
                payrollData.socialSecurityRate = new BigDecimal(rs.getString("SocialSecurity"));
                payrollData.medicareRate = new BigDecimal(rs.getString("Medicare"));
                payrollData.otherDeductions = new BigDecimal(rs.getString("OtherDeductions"));
                payrollData.pto = new BigDecimal(rs.getString("PTO"));
            } else {
                logError("User not found in the Payroll table for userID: " + userID);
                JOptionPane.showMessageDialog(null, "User not found.");
                return false;
            }
        } catch (SQLException e) {
            logError("Database error while fetching payroll details for userID: " + userID + ". Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error fetching payroll details: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void logError(String message) {
        System.err.println("[ERROR] " + message);
        // In a real-world scenario, integrate with a logging framework like Log4j or SLF4J
    }

    private void createFieldRow(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void saveTimeEntries(String userID, JTextField[] workedHoursFields, LocalDate payPeriodStart, PayrollData payrollData) {
        BigDecimal totalRegularHours = BigDecimal.ZERO;
        BigDecimal totalOvertimeHours = BigDecimal.ZERO;
        BigDecimal totalPTOHours = BigDecimal.ZERO;
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    
        try {
            // Check if a time entry already exists for the pay period
            boolean isLocked = false;
            int existingEntryID = -1;
    
            try (Connection connection = DatabaseHandler.connect();
                 PreparedStatement checkEntryStmt = connection.prepareStatement(
                         "SELECT EntryID, Locked FROM TimeEntries WHERE UserID = ? AND PayPeriodStart = ?")) {
                checkEntryStmt.setString(1, userID);
                checkEntryStmt.setDate(2, java.sql.Date.valueOf(payPeriodStart));
                ResultSet rs = checkEntryStmt.executeQuery();
    
                if (rs.next()) {
                    existingEntryID = rs.getInt("EntryID");
                    isLocked = rs.getBoolean("Locked");
                }
            }
    
            // If the entry is locked, notify the user and return
            if (isLocked) {
                JOptionPane.showMessageDialog(null, "The time entry for this pay period is locked and cannot be modified.", "Locked Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // Validate and calculate worked hours
            for (int i = 0; i < daysOfWeek.length; i++) {
                String text = workedHoursFields[i].getText();
                if (text == null || text.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all worked hours fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
    
                BigDecimal workedHours;
                try {
                    workedHours = new BigDecimal(text);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input for " + daysOfWeek[i] + ". Please enter a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
    
                if (workedHours.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(null, "Worked hours cannot be negative for " + daysOfWeek[i] + ".", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
    
                if ("Saturday".equals(daysOfWeek[i])) {
                    totalOvertimeHours = totalOvertimeHours.add(workedHours);
                } else {
                    totalOvertimeHours = totalOvertimeHours.add(calculateOvertimeHours(workedHours, daysOfWeek[i]));
                    totalRegularHours = totalRegularHours.add(workedHours.min(new BigDecimal("8")));
                }
            }
    
            try (Connection connection = DatabaseHandler.connect()) {
                // If an entry already exists, update it
                if (existingEntryID != -1) {
                    try (PreparedStatement updateStmt = connection.prepareStatement(
                            "UPDATE TimeEntries SET RegularHours = ?, OvertimeHours = ?, PTOHours = ? WHERE EntryID = ?")) {
                        updateStmt.setBigDecimal(1, totalRegularHours);
                        updateStmt.setBigDecimal(2, totalOvertimeHours);
                        updateStmt.setBigDecimal(3, totalPTOHours);
                        updateStmt.setInt(4, existingEntryID);
                        updateStmt.executeUpdate();
                    }
    
                    JOptionPane.showMessageDialog(null, "Time entry updated successfully!");
                } else {
                    // Otherwise, insert a new entry
                    try (PreparedStatement insertStmt = connection.prepareStatement(
                            "INSERT INTO TimeEntries (UserID, PayPeriodStart, RegularHours, OvertimeHours, PTOHours, Locked) VALUES (?, ?, ?, ?, ?, 0)")) {
                        insertStmt.setString(1, userID);
                        insertStmt.setDate(2, java.sql.Date.valueOf(payPeriodStart));
                        insertStmt.setBigDecimal(3, totalRegularHours);
                        insertStmt.setBigDecimal(4, totalOvertimeHours);
                        insertStmt.setBigDecimal(5, totalPTOHours);
                        insertStmt.executeUpdate();
                    }
    
                    JOptionPane.showMessageDialog(null, "Time entries for the pay period saved successfully!");
                }
    
                // Generate or update the payslip
                generatePaySlip(userID, payPeriodStart, payrollData, totalRegularHours, totalOvertimeHours, totalPTOHours);
            }
        } catch (Exception e) {
            logError("Error saving time entries for userID: " + userID + ". Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error saving time entries: " + e.getMessage());
        }
    }
    

    private void generatePaySlip(String userID, LocalDate payPeriodStart, PayrollData payrollData, BigDecimal totalRegularHours, BigDecimal totalOvertimeHours, BigDecimal totalPTOHours) {
        try (Connection connection = DatabaseHandler.connect()) {
            // Calculate gross pay
            BigDecimal regularPay = totalRegularHours.multiply(payrollData.hourlyRate);
            BigDecimal overtimePay = totalOvertimeHours.multiply(payrollData.hourlyRate.multiply(new BigDecimal("1.5")));
            BigDecimal grossPay = regularPay.add(overtimePay);
    
            // Calculate pre-tax deductions
            BigDecimal medicalDeduction = payrollData.getMedicalDeduction();
            BigDecimal preTaxPay = grossPay.subtract(medicalDeduction);
    
            // Calculate taxes
            BigDecimal federalTax = preTaxPay.multiply(payrollData.federalTaxRate);
            BigDecimal stateTax = preTaxPay.multiply(payrollData.stateTaxRate);
            BigDecimal socialSecurity = preTaxPay.multiply(payrollData.socialSecurityRate);
            BigDecimal medicare = preTaxPay.multiply(payrollData.medicareRate);
    
            // Calculate net pay
            BigDecimal dependentStipend = payrollData.getDependentStipend();
            BigDecimal netPay = preTaxPay.subtract(federalTax)
                                        .subtract(stateTax)
                                        .subtract(socialSecurity)
                                        .subtract(medicare)
                                        .add(dependentStipend);
    
            // Fetch the related payroll and time entry IDs
            String payrollID = null;
            String timeEntryID = null;
    
            try (PreparedStatement payrollStmt = connection.prepareStatement(
                    "SELECT PayrollID FROM Payroll WHERE UserID = ?")) {
                payrollStmt.setString(1, userID);
                ResultSet rs = payrollStmt.executeQuery();
                if (rs.next()) {
                    payrollID = rs.getString("PayrollID");
                }
            }
    
            try (PreparedStatement timeEntryStmt = connection.prepareStatement(
                    "SELECT EntryID FROM TimeEntries WHERE UserID = ? AND PayPeriodStart = ?")) {
                timeEntryStmt.setString(1, userID);
                timeEntryStmt.setDate(2, java.sql.Date.valueOf(payPeriodStart));
                ResultSet rs = timeEntryStmt.executeQuery();
                if (rs.next()) {
                    timeEntryID = rs.getString("EntryID");
                }
            }
    
            // Ensure we have valid IDs before proceeding
            if (payrollID == null || timeEntryID == null) {
                throw new SQLException("Failed to fetch PayrollID or TimeEntryID for user: " + userID);
            }
    
            // Check if a payslip already exists for this pay period
            boolean payslipExists = false;
            try (PreparedStatement checkPayslipStmt = connection.prepareStatement(
                    "SELECT PaySlipID FROM PaySlip WHERE UserID = ? AND PayPeriodStart = ?")) {
                checkPayslipStmt.setString(1, userID);
                checkPayslipStmt.setDate(2, java.sql.Date.valueOf(payPeriodStart));
                ResultSet rs = checkPayslipStmt.executeQuery();
                payslipExists = rs.next();
            }
    
            if (payslipExists) {
                // Update the existing payslip
                try (PreparedStatement updatePayslipStmt = connection.prepareStatement(
                        "UPDATE PaySlip SET TotalRegularHours = ?, TotalOvertimeHours = ?, TotalPTOHours = ?, " +
                                "GrossPay = ?, MedicalDeduction = ?, FederalTax = ?, StateTax = ?, SocialSecurity = ?, " +
                                "Medicare = ?, DependentStipend = ?, OtherDeductions = ?, NetPay = ? " +
                                "WHERE UserID = ? AND PayPeriodStart = ?")) {
                    updatePayslipStmt.setBigDecimal(1, totalRegularHours);
                    updatePayslipStmt.setBigDecimal(2, totalOvertimeHours);
                    updatePayslipStmt.setBigDecimal(3, totalPTOHours);
                    updatePayslipStmt.setBigDecimal(4, grossPay);
                    updatePayslipStmt.setBigDecimal(5, medicalDeduction);
                    updatePayslipStmt.setBigDecimal(6, federalTax);
                    updatePayslipStmt.setBigDecimal(7, stateTax);
                    updatePayslipStmt.setBigDecimal(8, socialSecurity);
                    updatePayslipStmt.setBigDecimal(9, medicare);
                    updatePayslipStmt.setBigDecimal(10, dependentStipend);
                    updatePayslipStmt.setBigDecimal(11, payrollData.getOtherDeductions());
                    updatePayslipStmt.setBigDecimal(12, netPay);
                    updatePayslipStmt.setString(13, userID);
                    updatePayslipStmt.setDate(14, java.sql.Date.valueOf(payPeriodStart));
    
                    updatePayslipStmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(null, "Payslip updated successfully!");
            } else {
                // Insert a new payslip
                try (PreparedStatement insertPayslipStmt = connection.prepareStatement(
                        "INSERT INTO PaySlip (UserID, PayrollID, TimeEntryID, PayPeriodStart, PayPeriodEnd, " +
                                "TotalRegularHours, TotalOvertimeHours, TotalPTOHours, GrossPay, MedicalDeduction, FederalTax, " +
                                "StateTax, SocialSecurity, Medicare, DependentStipend, OtherDeductions, NetPay) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    insertPayslipStmt.setString(1, userID);
                    insertPayslipStmt.setString(2, payrollID);
                    insertPayslipStmt.setString(3, timeEntryID);
                    insertPayslipStmt.setDate(4, java.sql.Date.valueOf(payPeriodStart));
                    insertPayslipStmt.setDate(5, java.sql.Date.valueOf(payPeriodStart.plusDays(6))); // Assuming a weekly pay period
                    insertPayslipStmt.setBigDecimal(6, totalRegularHours);
                    insertPayslipStmt.setBigDecimal(7, totalOvertimeHours);
                    insertPayslipStmt.setBigDecimal(8, totalPTOHours);
                    insertPayslipStmt.setBigDecimal(9, grossPay);
                    insertPayslipStmt.setBigDecimal(10, medicalDeduction);
                    insertPayslipStmt.setBigDecimal(11, federalTax);
                    insertPayslipStmt.setBigDecimal(12, stateTax);
                    insertPayslipStmt.setBigDecimal(13, socialSecurity);
                    insertPayslipStmt.setBigDecimal(14, medicare);
                    insertPayslipStmt.setBigDecimal(15, dependentStipend);
                    insertPayslipStmt.setBigDecimal(16, payrollData.getOtherDeductions());
                    insertPayslipStmt.setBigDecimal(17, netPay);
    
                    insertPayslipStmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(null, "Payslip generated successfully!");
            }
        } catch (SQLException e) {
            logError("Error generating or updating payslip for userID: " + userID + ". Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error generating or updating payslip: " + e.getMessage());
        }
    }
    
    
    


    private BigDecimal calculateOvertimeHours(BigDecimal workedHours, String dayOfWeek) {
        if (dayOfWeek.equals("Saturday") || workedHours.compareTo(new BigDecimal("8")) > 0) {
            return workedHours.subtract(new BigDecimal("8")).max(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }
}

class WorkedHoursInputVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
        String text = ((JTextField) input).getText();
        if (text.isEmpty()) {
            return true;
        }
        try {
            BigDecimal value = new BigDecimal(text);
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(null, "Worked hours cannot be negative.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}

class PayrollData {
    public boolean isSalary;
    public BigDecimal hourlyRate;
    public BigDecimal federalTaxRate;
    public BigDecimal stateTaxRate;
    public BigDecimal socialSecurityRate;
    public BigDecimal medicareRate;
    public BigDecimal otherDeductions;
    public String medicalCoverage;
    public int dependents;
    public BigDecimal pto; // Field for PTO hours

    private PayrollData() {
        this.pto = BigDecimal.ZERO; // Initialize PTO to 0 by default
    }

    public static PayrollData initialize() {
        return new PayrollData();
    }

    public BigDecimal getMedicalDeduction() {
        return medicalCoverage.equals("Single") ? new BigDecimal("50") : new BigDecimal("100");
    }

    public BigDecimal getDependentStipend() {
        return new BigDecimal(dependents).multiply(new BigDecimal("45"));
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    // Getter for PTO
    public BigDecimal getPTO() {
        return pto;
    }

    // Setter for PTO
    public void setPTO(BigDecimal pto) {
        if (pto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("PTO hours cannot be negative.");
        }
        this.pto = pto;
    }

    // Add PTO adjustment method for convenience
    public void adjustPTO(BigDecimal adjustment) {
        this.pto = this.pto.subtract(adjustment);
        if (this.pto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient PTO hours.");
        }
    }
}

