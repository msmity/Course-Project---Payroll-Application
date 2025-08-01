package main.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHandler {
    private static final Logger logger = Logger.getLogger(DatabaseHandler.class.getName());
    private static String databaseUrl = "jdbc:sqlite:db/PayrollDB.db";

    static {
        try {

            Class.forName("org.sqlite.JDBC"); // Ensure SQLite driver is loaded
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Database driver not found: {0}", e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during static initialization: {0}", e.getMessage());
        }
    }

    public static Connection connect() {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl);
            logger.info("Database connection established.");
            return connection;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection failed: {0}", e.getMessage());
            return null;
        }
    }

    public static void close(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    logger.info("Database connection closed.");
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to close the database connection: {0}", e.getMessage());
            }
        }
    }

    public static void setDatabaseUrl(String url) {
        databaseUrl = url;
        logger.info("Database URL updated to: " + url);
    }
}
