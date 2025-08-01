package main.Database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Login.Security;

public class DatabaseSetup {
    private static final Logger logger = Logger.getLogger(DatabaseSetup.class.getName());
    private static final String SCHEMA_FILE_PATH = "db/schema.sql"; // Hardcoded schema file path

    public static void executeSchema(String schemaFilePath) {
        Connection connection = DatabaseHandler.connect();
        if (connection == null) {
            logger.severe("Failed to connect to the database for schema execution.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(schemaFilePath));
             Statement stmt = connection.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sql.append(line);
                if (line.trim().endsWith(";")) { // Execute complete SQL statements
                    stmt.execute(sql.toString());
                    sql.setLength(0); // Reset the StringBuilder
                }
            }

            logger.info("Schema executed successfully from " + schemaFilePath);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading schema file: " + schemaFilePath, e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing schema: " + schemaFilePath, e);
        } finally {
            DatabaseHandler.close(connection);
        }
    }

    public static void seedData() {
        Connection connection = DatabaseHandler.connect();
        if (connection == null) {
            logger.severe("Failed to connect to the database for data seeding.");
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            // Clear existing data
            String[] tables = {"Users", "Payroll", "TimeEntries", "PaySlip"};
            for (String table : tables) {
                stmt.execute("DELETE FROM " + table + ";");
            }

            logger.info("Existing data cleared successfully.");
            
            // Seed Users
            stmt.execute("INSERT INTO Users (UserID, Role, Department, JobTitle, FirstName, LastName, SurName, " +
                "Status, DateOfBirth, Gender, Email, AddressLine1, AddressLine2, City, State, Zip, Password) VALUES " +
                "('HR0001', 'Admin', NULL, NULL, 'Admin', 'User', NULL, 'Active', '01/01/1980', 'Male', 'admin@abccompany.com', " +
                "'123 Main St', NULL, 'Springfield', 'IL', '62701', '" + Security.generateDefaultPassword("admin@abccompany.com", "01/01/1980") + "')," +
                "('EM0001','Employee', 'HR', 'Manager', 'Jane', 'Doe', NULL, 'Active', '02/15/1990', 'Female', 'jane.doe@abccompany.com', " +
                "'456 Elm St', NULL, 'Chicago', 'IL', '60601', '" + Security.generateDefaultPassword("jane.doe@abccompany.com", "02/15/1990") + "')," +
                "('EM0002','Employee', 'Finance', 'Analyst', 'John', 'Smith', NULL, 'Active', '03/10/1985', 'Male', 'john.smith@abccompany.com', " +
                "'789 Oak St', NULL, 'Peoria', 'IL', '61602', '" + Security.generateDefaultPassword("john.smith@abccompany.com", "03/10/1985") + "')," +
                "('EM0003','Employee', 'IT', 'Developer', 'Emily', 'Davis', NULL, 'Active', '05/05/1995', 'Female', 'emily.davis@abccompany.com', " +
                "'321 Pine St', NULL, 'Naperville', 'IL', '60540', '" + Security.generateDefaultPassword("emily.davis@abccompany.com", "05/05/1995") + "')," +
                "('EM0004','Employee', 'Sales', 'Executive', 'Michael', 'Brown', NULL, 'Active', '07/20/1988', 'Male', 'michael.brown@abccompany.com', " +
                "'654 Maple St', NULL, 'Aurora', 'IL', '60505', '" + Security.generateDefaultPassword("michael.brown@abccompany.com", "07/20/1988") + "')," +
                "('EM0005','Employee', 'Operations', 'Supervisor', 'Laura', 'Johnson', NULL, 'Active', '09/25/1983', 'Female', 'laura.johnson@abccompany.com', " +
                "'987 Birch St', NULL, 'Rockford', 'IL', '61101', '" + Security.generateDefaultPassword("laura.johnson@abccompany.com", "09/25/1983") + "')," +
                "('EM0006','Employee', 'Legal', 'Attorney', 'David', 'Garcia', NULL, 'Active', '11/11/1987', 'Male', 'david.garcia@abccompany.com', " +
                "'147 Ash St', NULL, 'Decatur', 'IL', '62521', '" + Security.generateDefaultPassword("david.garcia@abccompany.com", "11/11/1987") + "')," +
                "('EM0007','Employee', 'Marketing', 'Coordinator', 'Sophia', 'Martinez', NULL, 'Active', '06/30/1992', 'Female', 'sophia.martinez@abccompany.com', " +
                "'258 Cedar St', NULL, 'Evanston', 'IL', '60201', '" + Security.generateDefaultPassword("sophia.martinez@abccompany.com", "06/30/1992") + "')," +
                "('EM0008','Employee', 'Logistics', 'Manager', 'Daniel', 'Lopez', NULL, 'Active', '12/15/1989', 'Male', 'daniel.lopez@abccompany.com', " +
                "'369 Walnut St', NULL, 'Springfield', 'IL', '62701', '" + Security.generateDefaultPassword("daniel.lopez@abccompany.com", "12/15/1989") + "')," +
                "('EM0009','Employee', 'R&D', 'Scientist', 'Olivia', 'Wilson', NULL, 'Active', '01/22/1991', 'Female', 'olivia.wilson@abccompany.com', " +
                "'123 Chestnut St', NULL, 'Carbondale', 'IL', '62901', '" + Security.generateDefaultPassword("olivia.wilson@abccompany.com", "01/22/1991") + "')," +
                "('EM0010','Employee', 'Support', 'Technician', 'William', 'Lee', NULL, 'Active', '08/18/1993', 'Male', 'william.lee@abccompany.com', " +
                "'456 Poplar St', NULL, 'Champaign', 'IL', '61820', '" + Security.generateDefaultPassword("william.lee@abccompany.com", "08/18/1993") + "')," +
                "('EM0011','Employee', 'Quality', 'Inspector', 'Emma', 'White', NULL, 'Active', '03/05/1994', 'Female', 'emma.white@abccompany.com', " +
                "'789 Spruce St', NULL, 'Quincy', 'IL', '62301', '" + Security.generateDefaultPassword("emma.white@abccompany.com", "03/05/1994") + "');");
            
                stmt.execute("INSERT INTO Payroll (UserID, PayType, HourlyRate, FederalTax, StateTax, SocialSecurity, Medicare, OtherDeductions, Dependents, MedicalCoverage, PTO) VALUES " +
            "('HR0001', 'Salary', 50.0, 0.0765, 0.0315, 0.062, 0.0145, 0.0, 0, 'Single', 120)," +
            "('EM0001', 'Salary', 40.0, 0.0765, 0.0315, 0.062, 0.0145, 0.0, 2, 'Family', 100)," +
            "('EM0002', 'Hourly', 25.0, 0.0765, 0.0315, 0.062, 0.0145, 10.0, 1, 'Single', 80)," +
            "('EM0003', 'Hourly', 30.0, 0.0765, 0.0315, 0.062, 0.0145, 5.0, 0, 'Single', 90)," +
            "('EM0004', 'Salary', 45.0, 0.0765, 0.0315, 0.062, 0.0145, 0.0, 3, 'Family', 110)," +
            "('EM0005', 'Hourly', 22.5, 0.0765, 0.0315, 0.062, 0.0145, 7.0, 2, 'Family', 85)," +
            "('EM0006', 'Salary', 60.0, 0.0765, 0.0315, 0.062, 0.0145, 0.0, 1, 'Single', 130)," +
            "('EM0007', 'Hourly', 28.0, 0.0765, 0.0315, 0.062, 0.0145, 8.0, 0, 'Single', 75)," +
            "('EM0008', 'Salary', 55.0, 0.0765, 0.0315, 0.062, 0.0145, 0.0, 4, 'Family', 140)," +
            "('EM0009', 'Hourly', 35.0, 0.0765, 0.0315, 0.062, 0.0145, 6.0, 1, 'Single', 95)," +
            "('EM0010', 'Salary', 48.0, 0.0765, 0.0315, 0.062, 0.0145, 0.0, 2, 'Family', 100)," +
            "('EM0011', 'Hourly', 26.0, 0.0765, 0.0315, 0.062, 0.0145, 9.0, 1, 'Single', 80);");

        // Seed TimeEntries
        stmt.execute("INSERT INTO TimeEntries (UserID, PayPeriodStart, RegularHours, OvertimeHours, PTOHours, Locked, HourlyRate) VALUES " +
            "('HR0001', '1734238800000', 160, 0, 8, 1, 50.0)," +
            "('EM0001', '1734238800000', 160, 10, 6, 1, 40.0)," +
            "('EM0002', '1734238800000', 140, 15, 8, 0, 25.0)," +
            "('EM0003', '1734238800000', 150, 5, 12, 0, 30.0)," +
            "('EM0004', '1734238800000', 160, 20, 10, 1, 45.0)," +
            "('EM0005', '1734238800000', 120, 25, 15, 0, 22.5)," +
            "('EM0006', '1734238800000', 160, 0, 20, 1, 60.0)," +
            "('EM0007', '1734238800000', 140, 18, 5, 0, 28.0)," +
            "('EM0008', '1734238800000', 160, 12, 7, 1, 55.0)," +
            "('EM0009', '1734238800000', 150, 10, 6, 0, 35.0)," +
            "('EM0010', '1734238800000', 160, 5, 8, 1, 48.0)," +
            "('EM0011', '1734238800000', 135, 20, 10, 0, 26.0);");

        // Seed PaySlip
        stmt.execute("INSERT INTO PaySlip (UserID, PayrollID, TimeEntryID, PayPeriodStart, PayPeriodEnd, TotalRegularHours, TotalOvertimeHours, TotalPTOHours, GrossPay, MedicalDeduction, FederalTax, StateTax, SocialSecurity, Medicare, DependentStipend, OtherDeductions, NetPay) VALUES " +
            "('HR0001', 1, 1, '1734238800000', '1734757200000', 160, 0, 8, 8000, 300, 612, 252, 496, 116, 0, 0, 6224)," +
            "('EM0001', 2, 2, '1734238800000', '1734757200000', 160, 10, 6, 7200, 600, 550, 226, 446, 104, 400, 0, 4874)," +
            "('EM0002', 3, 3, '1734238800000', '1734757200000', 140, 15, 8, 4125, 150, 315, 129, 256, 60, 200, 10, 3005)," +
            "('EM0003', 4, 4, '1734238800000', '1734757200000', 150, 5, 12, 4875, 150, 368, 151, 303, 72, 0, 5, 3826)," +
            "('EM0004', 5, 5, '1734238800000', '1734757200000', 160, 20, 10, 9200, 600, 703, 289, 572, 136, 600, 0, 5900)," +
            "('EM0005', 6, 6, '1734238800000', '1734757200000', 120, 25, 15, 4200, 450, 321, 132, 260, 62, 400, 7, 2568)," +
            "('EM0006', 7, 7, '1734238800000', '1734757200000', 160, 0, 20, 9600, 150, 732, 301, 595, 141, 200, 0, 7481)," +
            "('EM0007', 8, 8, '1734238800000', '1734757200000', 140, 18, 5, 5020, 150, 383, 158, 311, 74, 0, 8, 3936)," +
            "('EM0008', 9, 9, '1734238800000', '1734757200000', 160, 12, 7, 9900, 600, 756, 310, 614, 145, 800, 0, 6675)," +
            "('EM0009', 10, 10, '1734238800000', '1734757200000', 150, 10, 6, 5775, 150, 441, 182, 358, 84, 200, 6, 4354)," +
            "('EM0010', 11, 11, '1734238800000', '1734757200000', 160, 5, 8, 7680, 450, 585, 241, 475, 112, 400, 0, 5417)," +
            "('EM0011', 12, 12, '1734238800000', '1734757200000', 135, 20, 10, 5400, 150, 410, 169, 335, 79, 200, 9, 4048);");


            System.out.println("Data seeded successfully.");

            logger.info("Database seeding completed successfully.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error seeding data: ", e);
        } finally {
            DatabaseHandler.close(connection);
        }
    }

    public static void main(String[] args) {
        try {
            logger.info("Starting schema execution...");
            executeSchema(SCHEMA_FILE_PATH);

            logger.info("Starting data seeding...");
            seedData();

            logger.info("Database setup completed.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in database setup: ", e);
        }
    }
}
