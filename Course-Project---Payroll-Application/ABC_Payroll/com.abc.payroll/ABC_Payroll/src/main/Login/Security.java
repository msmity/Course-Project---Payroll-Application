package main.Login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import main.Database.DatabaseHandler;

public class Security {

    /**
     * Generates an automated default password based on email and date of birth.
     * @param email The employee's email.
     * @param dob The employee's date of birth (MM/DD/YYYY).
     * @return A hashed password.
     */
    public static String generateDefaultPassword(String email, String dob) {
        if (!validateEmail(email) || !validateDate(dob)) {
            throw new IllegalArgumentException("Invalid email or date of birth format.");
        }
    
        // Extract the part of the email before the '@'
        String usernamePart = email.split("@")[0];
    
        // Concatenate the username part with the date of birth
        String rawPassword = usernamePart + dob;
    
        // Return the hashed password
        return hashPassword(rawPassword);
    }
    

    /**
     * Hashes a password using SHA-256.
     * @param password The plain text password.
     * @return The hashed password as a hexadecimal string.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage());
        }
    }

    /**
     * Validates login credentials based on the unified Users table.
     * @param userId The user's email or UserID.
     * @param plainPassword The plain text password entered by the user.
     * @return True if credentials are valid, false otherwise.
     */
    public static boolean validateLogin(String userId, String plainPassword) {
        if (userId == null || plainPassword == null || userId.trim().isEmpty() || plainPassword.trim().isEmpty()) {
            return false;
        }
        String hashedPassword = hashPassword(plainPassword);
        String query = "SELECT Password FROM Users WHERE Email = ? OR UserID = ?";

        try (Connection connection = DatabaseHandler.connect();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, userId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("Password");
                return storedPassword.equals(hashedPassword);
            }

        } catch (Exception e) {
            System.err.println("Login validation error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Validates and retrieves the role for a given userId.
     * @param userId The user's UserID.
     * @return The role of the user if found, or null if no role is found or an error occurs.
     */
    public static String validateRole(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT Role FROM Users WHERE UserID = ?";

        try (Connection connection = DatabaseHandler.connect();
            PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Role");
            }

        } catch (Exception e) {
            System.err.println("Role validation error: " + e.getMessage());
        }

        return null;
    }


    /**
     * Validates if a string is a valid email.
     * @param email The email to validate.
     * @return True if the email is valid, false otherwise.
     */
    public static boolean validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && Pattern.matches(emailRegex, email);
    }

    /**
     * Validates if a string is a valid date in the format MM/DD/YYYY.
     * Ensures that the date is logical (e.g., not in the future and at least 18 years old).
     * @param date The date string to validate.
     * @return True if the date is valid, false otherwise.
     */
    public static boolean validateDate(String date) {
        String dateRegex = "^(0[1-9]|1[0-2])/([0-2][0-9]|3[01])/\\d{4}$";
        if (!Pattern.matches(dateRegex, date)) {
            return false;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate parsedDate = LocalDate.parse(date, formatter);
            LocalDate now = LocalDate.now();
            if (parsedDate.isAfter(now)) {
                return false;
            }
            int age = Period.between(parsedDate, now).getYears();
            return age >= 18;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validates if a name contains only letters and spaces.
     * @param name The name to validate.
     * @return True if the name is valid, false otherwise.
     */
    public static boolean validateName(String name) {
        return name != null && Pattern.matches("^[A-Za-z ]+$", name);
    }

    /**
     * Validates if a ZIP code is a valid 5-digit code.
     * @param zip The ZIP code to validate.
     * @return True if the ZIP code is valid, false otherwise.
     */
    public static boolean validateZip(String zip) {
        return zip != null && Pattern.matches("^\\d{5}$", zip);
    }

    /**
     * Validates gender input to ensure it matches 'Male' or 'Female'.
     * @param gender The gender to validate.
     * @return True if valid, false otherwise.
     */
    public static boolean validateGender(String gender) {
        return gender != null && (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female"));
    }
    /**
     * Validates medical coverage input.
     * @param medicalCoverage The medical coverage to validate.
     * @return True if valid, false otherwise.
     */
    public static boolean validateMedicalCoverage(String medicalCoverage) {
        return medicalCoverage != null && (medicalCoverage.equalsIgnoreCase("Single") || medicalCoverage.equalsIgnoreCase("Family"));
    }
}
