package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {

                String url = "jdbc:mysql://localhost:3306/hospital_db";
                String user = "root";
                String password = "Dewanshi@123";

                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Connected to DB");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}