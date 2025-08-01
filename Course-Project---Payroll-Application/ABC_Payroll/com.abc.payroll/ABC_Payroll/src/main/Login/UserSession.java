package main.Login;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class UserSession {
    private static final Logger logger = Logger.getLogger(UserSession.class.getName());
    private static String userId;
    private static String role;
    private static LocalDateTime sessionStart;

    /**
     * Sets the user session.
     * @param id The user's ID.
     * @param userRole The user's role.
     */
    public static void setUser(String id, String userRole) {
        userId = id;
        role = userRole;
        sessionStart = LocalDateTime.now();
        logger.info("User session started for user ID: " + userId + ", Role: " + role);
    }

    /**
     * Gets the current session user ID.
     * @return The user ID.
     */
    public static String getUserId() {
        return userId;
    }

    /**
     * Gets the current session user role.
     * @return The user role.
     */
    public static String getRole() {
        return role;
    }

    /**
     * Gets the session start time.
     * @return The session start time.
     */
    public static LocalDateTime getSessionStart() {
        return sessionStart;
    }

    /**
     * Validates if the current user has access to a specific feature.
     * @param feature The feature being accessed.
     * @return True if the user has access, false otherwise.
     */
    public static boolean hasAccess(String feature) {
        if ("Admin".equalsIgnoreCase(role)) {
            return true; // Admins have access to all features
        } else if ("Employee".equalsIgnoreCase(role)) {
            return feature.equalsIgnoreCase("Enter Hours") ||
                   feature.equalsIgnoreCase("View PTO") ||
                   feature.equalsIgnoreCase("View Paycheck");
        }
        logger.warning("Access denied for user ID: " + userId + " to feature: " + feature);
        return false;
    }

    /**
     * Checks if the session has expired.
     * @return True if the session is expired, false otherwise.
     */
    public static boolean isSessionExpired() {
        if (sessionStart == null) {
            return true;
        }
        Duration duration = Duration.between(sessionStart, LocalDateTime.now());
        boolean expired = duration.toMinutes() > 30; // Example: 30-minute timeout
        if (expired) {
            logger.info("Session expired for user ID: " + userId);
        }
        return expired;
    }

    /**
     * Clears the session data (logout or session expiration).
     */
    public static void resetSession() {
        logger.info("Resetting session for user ID: " + userId);
        userId = null;
        role = null;
        sessionStart = null;
    }

    /**
     * Logs out the current user and resets the session.
     */
    public static void logout() {
        if (userId != null) {
            logger.info("User ID " + userId + " logging out.");
        }
        resetSession();
    }
}
