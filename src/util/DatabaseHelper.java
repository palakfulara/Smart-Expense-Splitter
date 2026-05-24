package util;

import java.sql.*;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:expenses.db";
    private Connection conn;

    public DatabaseHelper() {
        connect();
        createTables();
    }

    private void connect() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to database.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private void createTables() {
        String trips = "CREATE TABLE IF NOT EXISTS trips (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL);";

        String members = "CREATE TABLE IF NOT EXISTS members (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "trip_id INTEGER," +
                "FOREIGN KEY(trip_id) REFERENCES trips(id));";

        String expenses = "CREATE TABLE IF NOT EXISTS expenses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "description TEXT," +
                "amount REAL," +
                "paid_by TEXT," +
                "split_type TEXT," +
                "trip_id INTEGER," +
                "FOREIGN KEY(trip_id) REFERENCES trips(id));";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(trips);
            stmt.execute(members);
            stmt.execute(expenses);
        } catch (SQLException e) {
            System.out.println("Table creation failed: " + e.getMessage());
        }
    }

    // Save a new trip and return its generated ID
    public int saveTrip(String name) {
        String sql = "INSERT INTO trips(name) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Save trip failed: " + e.getMessage());
        }
        return -1;
    }

    // Save a member under a trip
    public void saveMember(String name, int tripId) {
        String sql = "INSERT INTO members(name, trip_id) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, tripId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Save member failed: " + e.getMessage());
        }
    }

    // Save an expense under a trip
    public void saveExpense(String description, double amount, String paidBy, String splitType, int tripId) {
        String sql = "INSERT INTO expenses(description, amount, paid_by, split_type, trip_id) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, description);
            ps.setDouble(2, amount);
            ps.setString(3, paidBy);
            ps.setString(4, splitType);
            ps.setInt(5, tripId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Save expense failed: " + e.getMessage());
        }
    }

    // Get all trips
    public ResultSet getAllTrips() {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery("SELECT * FROM trips");
        } catch (SQLException e) {
            System.out.println("Get trips failed: " + e.getMessage());
            return null;
        }
    }

    // Get all members of a trip
    public ResultSet getMembersForTrip(int tripId) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM members WHERE trip_id = ?");
            ps.setInt(1, tripId);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.out.println("Get members failed: " + e.getMessage());
            return null;
        }
    }

    // Get all expenses of a trip
    public ResultSet getExpensesForTrip(int tripId) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE trip_id = ?");
            ps.setInt(1, tripId);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.out.println("Get expenses failed: " + e.getMessage());
            return null;
        }
    }

    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println("Close failed: " + e.getMessage());
        }
    }
}