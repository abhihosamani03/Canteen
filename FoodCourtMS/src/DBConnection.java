import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection provides a centralized database connection helper
 * for the Food Court Management System.
 */
public class DBConnection {
    // Database URL, Port, and Database Name
    private static final String URL = "jdbc:mysql://localhost:3306/food_court_db?useSSL=false&allowPublicKeyRetrieval=true";
    
    // Change username and password as per your MySQL Workbench configuration
    private static final String USER = "root";
    private static final String PASSWORD = "your_mysql_password";

    static {
        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found. Make sure mysql-connector-java.jar is in the classpath.");
            e.printStackTrace();
        }
    }

    /**
     * Establishes and returns a connection to the database.
     * @return Connection object
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
