import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class HotelManagementApp {

    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_management?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Kartik@18";

    private static Connection conn;
    private static Scanner scanner = new Scanner(System.in);
    // Scheduler for background tasks (demonstrates multithreading)
    private static ScheduledExecutorService scheduler;
    private static final int MONITOR_INTERVAL_SECONDS = 30; // run every 30 seconds

    public static void main(String[] args) {
        try {
            // Connect to DB
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to DB.");

            // Create tables
            createTables();

            // Start background monitor thread to update reservation statuses
            startMonitor();

            // Main menu loop
            while (true) {
                System.out.println("\n--- Hotel Management Menu ---");
                System.out.println("1. Add Hotel");
                System.out.println("2. Add Room");
                System.out.println("3. Add Guest");
                System.out.println("4. Make Reservation");
                System.out.println("5. List Reservations");
                System.out.println("6. Exit");
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        addHotel();
                        break;
                    case 2:
                        addRoom();
                        break;
                    case 3:
                        addGuest();
                        break;
                    case 4:
                        makeReservation();
                        break;
                    case 5:
                        listReservations();
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        // stop background monitor and close resources before exiting
                        stopMonitor();
                        if (conn != null && !conn.isClosed()) {
                            conn.close();
                        }
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Fatal DB error: " + e.getMessage());
        } finally {
            // Ensure background monitor and resources are stopped/closed on exit or error
            try {
                stopMonitor();
            } catch (Throwable t) {
                System.err.println("Error stopping monitor: " + t.getMessage());
            }

            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException se) {
                System.err.println("Error closing main DB connection: " + se.getMessage());
            }

            try {
                if (scanner != null) {
                    scanner.close();
                }
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    private static void createTables() throws SQLException {
        Statement stmt = conn.createStatement();

        String createHotels = "CREATE TABLE IF NOT EXISTS Hotels (\n"
                + "    hotel_id INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    name VARCHAR(100) NOT NULL,\n"
                + "    address VARCHAR(255),\n"
                + "    city VARCHAR(50),\n"
                + "    state VARCHAR(50),\n"
                + "    country VARCHAR(50),\n"
                + "    phone VARCHAR(20)\n"
                + ");";

        String createRooms = "CREATE TABLE IF NOT EXISTS Rooms (room_id INT AUTO_INCREMENT PRIMARY KEY,hotel_id INT,room_number VARCHAR(10) NOT NULL,room_type VARCHAR(50),price DECIMAL(10,2),status ENUM('available', 'occupied', 'maintenance') DEFAULT 'available',FOREIGN KEY (hotel_id) REFERENCES Hotels(hotel_id))";

        String createGuests = "CREATE TABLE IF NOT EXISTS Guests (\n"
                + "    guest_id INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    first_name VARCHAR(50),\n"
                + "    last_name VARCHAR(50),\n"
                + "    email VARCHAR(100),\n"
                + "    phone VARCHAR(20)\n"
                + ");";

        String createReservations = "CREATE TABLE IF NOT EXISTS Reservations (\n"
                + "    reservation_id INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    guest_id INT,\n"
                + "    room_id INT,\n"
                + "    check_in_date DATE,\n"
                + "    check_out_date DATE,\n"
                + "    status ENUM('booked', 'checked_in', 'checked_out', 'cancelled') DEFAULT 'booked',\n"
                + "    FOREIGN KEY (guest_id) REFERENCES Guests(guest_id),\n"
                + "    FOREIGN KEY (room_id) REFERENCES Rooms(room_id)\n"
                + ");";

        stmt.executeUpdate(createHotels);
        stmt.executeUpdate(createRooms);
        stmt.executeUpdate(createGuests);
        stmt.executeUpdate(createReservations);

        System.out.println("Tables created or verified.");
    }

    private static void addHotel() throws SQLException {
        System.out.print("Enter hotel name: ");
        String name = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Enter city: ");
        String city = scanner.nextLine();
        System.out.print("Enter state: ");
        String state = scanner.nextLine();
        System.out.print("Enter country: ");
        String country = scanner.nextLine();
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();

        String sql = "INSERT INTO Hotels (name, address, city, state, country, phone) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, city);
            ps.setString(4, state);
            ps.setString(5, country);
            ps.setString(6, phone);
            ps.executeUpdate();
            System.out.println("Hotel added successfully.");
        }
    }

    private static void addRoom() throws SQLException {
        System.out.print("Enter hotel ID: ");
        int hotelId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter room number: ");
        String roomNumber = scanner.nextLine();
        System.out.print("Enter room type (e.g., Single, Double): ");
        String roomType = scanner.nextLine();
        System.out.print("Enter price: ");
        double price = Double.parseDouble(scanner.nextLine());

        String sql = "INSERT INTO Rooms (hotel_id, room_number, room_type, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hotelId);
            ps.setString(2, roomNumber);
            ps.setString(3, roomType);
            ps.setDouble(4, price);
            ps.executeUpdate();
            System.out.println("Room added successfully.");
        }
    }

    private static void addGuest() throws SQLException {
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();

        String sql = "INSERT INTO Guests (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.executeUpdate();
            System.out.println("Guest added successfully.");
        }
    }

    private static void makeReservation() throws SQLException {
        try {
            System.out.print("Enter guest ID: ");
            int guestId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter room ID: ");
            int roomId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter check-in date (YYYY-MM-DD): ");
            String checkIn = scanner.nextLine().trim();
            System.out.print("Enter check-out date (YYYY-MM-DD): ");
            String checkOut = scanner.nextLine().trim();

            // Validate guest exists
            String guestExistsSql = "SELECT 1 FROM Guests WHERE guest_id = ?";
            try (PreparedStatement psGuest = conn.prepareStatement(guestExistsSql)) {
                psGuest.setInt(1, guestId);
                try (ResultSet rs = psGuest.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No guest found with ID " + guestId + ". Please add the guest first.");
                        return;
                    }
                }
            }

            // Validate room exists
            String roomExistsSql = "SELECT 1 FROM Rooms WHERE room_id = ?";
            try (PreparedStatement psRoom = conn.prepareStatement(roomExistsSql)) {
                psRoom.setInt(1, roomId);
                try (ResultSet rs = psRoom.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No room found with ID " + roomId + ". Please add the room first.");
                        return;
                    }
                }
            }

            // Validate dates
            java.time.LocalDate inDate;
            java.time.LocalDate outDate;
            try {
                inDate = java.time.LocalDate.parse(checkIn);
                outDate = java.time.LocalDate.parse(checkOut);
            } catch (java.time.format.DateTimeParseException dtpe) {
                System.out.println("Invalid date format. Use YYYY-MM-DD.");
                return;
            }
            if (!outDate.isAfter(inDate)) {
                System.out.println("Check-out date must be after check-in date.");
                return;
            }

            // Check if room is available in the given period (simplified)
            String checkAvailabilitySql = "SELECT COUNT(*) FROM Reservations\n"
                    + "WHERE room_id = ? AND status IN ('booked', 'checked_in')\n"
                    + "AND (check_in_date < ? AND check_out_date > ?)";

            try (PreparedStatement psCheck = conn.prepareStatement(checkAvailabilitySql)) {
                psCheck.setInt(1, roomId);
                psCheck.setDate(2, Date.valueOf(outDate));
                psCheck.setDate(3, Date.valueOf(inDate));
                try (ResultSet rs = psCheck.executeQuery()) {
                    rs.next();
                    int count = rs.getInt(1);

                    if (count > 0) {
                        System.out.println("Room is not available for the selected dates.");
                        return;
                    }
                }
            }

            String sql = "INSERT INTO Reservations (guest_id, room_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, guestId);
                ps.setInt(2, roomId);
                ps.setDate(3, Date.valueOf(inDate));
                ps.setDate(4, Date.valueOf(outDate));
                ps.executeUpdate();
                System.out.println("Reservation made successfully.");
            } catch (SQLIntegrityConstraintViolationException icve) {
                // defensive: this shouldn't normally happen because we validated, but handle gracefully
                System.out.println("Could not make reservation: referenced guest or room does not exist (FK constraint). Please verify IDs.");
            }

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid numeric input for guest or room ID.");
        }
    }

    private static void listReservations() throws SQLException {
        String sql = "SELECT r.reservation_id, g.first_name, g.last_name, rm.room_number, r.check_in_date, r.check_out_date, r.status\n"
                + "FROM Reservations r\n"
                + "JOIN Guests g ON r.guest_id = g.guest_id\n"
                + "JOIN Rooms rm ON r.room_id = rm.room_id\n"
                + "ORDER BY r.check_in_date DESC";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n--- Reservations ---");
            while (rs.next()) {
                System.out.printf("Reservation ID: %d | Guest: %s %s | Room: %s | Check-in: %s | Check-out: %s | Status: %s%n",
                        rs.getInt("reservation_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("room_number"),
                        rs.getDate("check_in_date"),
                        rs.getDate("check_out_date"),
                        rs.getString("status"));
            }
        }
    }

    // Start the background monitor which periodically updates reservation statuses.
    private static void startMonitor() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Reservation-Monitor");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                runMonitorTask();
            } catch (Throwable t) {
                System.err.println("Monitor task error: " + t.getMessage());
            }
        }, 0, MONITOR_INTERVAL_SECONDS, TimeUnit.SECONDS);
        System.out.println("Background reservation monitor started (every " + MONITOR_INTERVAL_SECONDS + "s).");
    }

    // Stop the background monitor gracefully.
    private static void stopMonitor() {
        if (scheduler == null) return;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Background reservation monitor stopped.");
    }

    // The actual monitor logic: uses its own DB connection to avoid contention with main thread.
    private static void runMonitorTask() {
        LocalDate today = LocalDate.now();
        java.sql.Date sqlToday = java.sql.Date.valueOf(today);

        String checkInSql = "UPDATE Reservations SET status='checked_in' WHERE status='booked' AND check_in_date <= ? AND check_out_date >= ?";
        String checkOutSql = "UPDATE Reservations SET status='checked_out' WHERE status IN ('booked','checked_in') AND check_out_date < ?";

        try (Connection c = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // mark current stays as checked_in
            try (PreparedStatement ps = c.prepareStatement(checkInSql)) {
                ps.setDate(1, sqlToday);
                ps.setDate(2, sqlToday);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    System.out.println("[Monitor] Marked " + updated + " reservation(s) as checked_in.");
                }
            }

            // mark past stays as checked_out
            try (PreparedStatement ps2 = c.prepareStatement(checkOutSql)) {
                ps2.setDate(1, sqlToday);
                int updated2 = ps2.executeUpdate();
                if (updated2 > 0) {
                    System.out.println("[Monitor] Marked " + updated2 + " reservation(s) as checked_out.");
                }
            }

        } catch (SQLException e) {
            System.err.println("[Monitor] DB error: " + e.getMessage());
        }
    }
}
