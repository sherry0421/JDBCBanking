import java.sql.*;
import java.io.*;
import oracle.jdbc.*;
import oracle.sql.*;
import java.util.*;

public class Part1 {
    // use the same connection for all functions
    public static Connection conn = null;

    public static void main(String[] args) {
        try {
            // make a new driver manager and connection with thin driver
            DriverManager.registerDriver
                    (new oracle.jdbc.driver.OracleDriver());
            System.out.println("Connecting to JDBC...");
            conn = DriverManager.getConnection
                    ("jdbc:oracle:thin:@//localhost:1521/orcl", "system", "admin");
            System.out.println("JDBC connected.\n");

            // Create a statement
            Statement stmt = conn.createStatement();

            stmt.executeQuery("CREATE TABLE Branch ( " +
                    "B#       CHAR(3) PRIMARY KEY, " +
                    "Address  VARCHAR(10) UNIQUE)");
            System.out.println("Branch table created.");

            stmt.executeQuery("CREATE TABLE Customer ( " +
                    "C#       CHAR(5) PRIMARY KEY, " +
                    "Name     VARCHAR(10) UNIQUE, " +
                    "Status   INTEGER DEFAULT 0 CHECK(Status IN (0, 1, 2, 3)))");
            System.out.println("Customer table created.");

            stmt.executeQuery("CREATE TABLE Account ( " +
                    "B#       CHAR(3), " +
                    "A#       CHAR(4), " +
                    "C#       CHAR(5), " +
                    "Balance  FLOAT DEFAULT 0 CHECK(Balance>=0), " +
                    "PRIMARY KEY(B#, A#), " +
                    "FOREIGN KEY (B#) REFERENCES Branch(B#) ON DELETE CASCADE, " +
                    "FOREIGN KEY (C#) REFERENCES Customer(C#) ON DELETE CASCADE)");
            System.out.println("Account table created.");
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("SQL exception: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}