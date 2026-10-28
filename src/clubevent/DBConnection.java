package clubevent;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/clubevent_db";
    private static final String USER = "root";
    private static final String PASSWORD = "qwerty123";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
            return null;
        }
    }
}
