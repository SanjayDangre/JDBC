import java.sql.DriverManager; // class
import java.sql.Connection;  // interface
import java.sql.Statement;   // interface
import java.sql.ResultSet;   // interface
import java.sql.SQLException;  // class
import java.util.Scanner;  // class

public class Hotel_Reservation {
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "Pass@1234";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // drivers loaded here
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            // getConnection is a DriverManager class method which takes three arguments url, username and password
            // it hold the connection in the instance of Connection Interface
            Connection connection = DriverManager.getConnection(url, username, password);  // connection established here
            System.out.println("\t\tHOTEL RESERVATION SYSTEM\t");
            while (true) {
                System.out.println("1) Reserve a room");
                System.out.println("2) View Reservation");
                System.out.println("3) Get Room Number");
                System.out.println("4) Update Reservation");
                System.out.println("5) Delete Reservation");
                System.out.println("0) Exit");
                Scanner scanner = new Scanner(System.in);
                System.out.println("Choose an option: ");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        reservation(connection,scanner);
                        break;
                    case 2:
                        viewReservation(connection);
                        break;
                    case 3:
                        getNumber(connection,scanner);
                        break;
                    case 4:
                        updation(connection,scanner);
                        break;
                    case 5:
                        deletion(connection,scanner);
                        break;
                    case 0:
                        exit(); // it may throws the InterruptedException
                        return;
                    default:
                        System.out.println("Choose a valid option");
                }
            }
        } catch (SQLException j) {
            System.out.println(j.getMessage());
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public static void reservation(Connection connection, Scanner scanner) throws SQLException {
        try {
            System.out.println("Enter Guest name: ");
            String guestname = scanner.next();
            System.out.println("Enter Room Number: ");
            int roomnumber = scanner.nextInt();
            System.out.println("Enter Guest Contact Number: ");
            String contactnumber = scanner.next();

            String sql = "INSERT INTO reservation(guest_name,room_number,contact_number)" +
                    " VALUES('" + guestname + "'," + roomnumber + ",'" + contactnumber + "')";
            try (Statement statement = connection.createStatement()) {
                int affectedrows = statement.executeUpdate(sql);  // executeUpdate is used because of insertion of data and it return int value that's why we compared to 0
                if (affectedrows > 0) {
                    System.out.println("Reservation Successful");
                    System.out.println();
                } else {
                    System.out.println("Reservation Failed!!!");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void viewReservation(Connection connection) throws SQLException {
        // to view reservation table in command line client use "select * from reservation"
        String sql = "SELECT reservation_id,guest_name,room_number,contact_number,reservation_date FROM reservation";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) { // executeQuery return all the data from database
                    System.out.println("Current Reservations: ");

            while (resultSet.next()) {
                int reservationID = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contact_number = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();

                System.out.println("Reservation ID: " + reservationID + "\nGuest Name: " + guestName + "\nRoom Number: " + roomNumber + "\nContact Number: " + contact_number + "\nReservation Date: " + reservationDate);
                System.out.println();
            }
            statement.close();
            resultSet.close();
        }
    }

    public static void getNumber(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter Reservation ID: ");
            int reservationId = scanner.nextInt();
            System.out.println("Enter Guest Name: ");
            String guestName = scanner.next();

            String sql = "SELECT room_number FROM reservation " +
                    "WHERE reservation_id = " + reservationId +
                    " AND guest_name = '" + guestName + "'";

            try (Statement statement = connection.createStatement();
                 ResultSet resultset = statement.executeQuery(sql)) {

                if (resultset.next()) {
                    int roomNumber = resultset.getInt("room_number");
                    System.out.println("Room Number for Reservation ID: " + reservationId +
                            "\nGuest: " + guestName + "\nID: " + roomNumber);
                } else {
                    System.out.println("Reservation not found for the given ID and Guest Name");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updation(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter Reservation ID: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine();
            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the ID.");
                return;
            }
            System.out.println("Enter new Guest name: ");
            String newGuestName = scanner.next();
            System.out.println("Enter new Room Number: ");
            int newRoomNumber = scanner.nextInt();
            System.out.println("Enter new Contact Number: ");
            String newContactNumber = scanner.next();

            String sql = "UPDATE reservation SET guest_name = '" + newGuestName + "'," + " room_number = " + newRoomNumber + "," + " contact_number = '" + newContactNumber + "' " +
                    " WHERE reservation_id =" + reservationId;
            try (Statement statement = connection.createStatement()) {
                int affectedrows = statement.executeUpdate(sql);
                if (affectedrows > 0) {
                    System.out.println("Reservation updated successfully");
                } else {
                    System.out.println("Reservation updated failed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deletion(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter Reservation ID to delete: ");
            int reservationId = scanner.nextInt();

            if (!reservationExists(connection, reservationId)) {
                System.out.println("No Reservation found for this ID");
                return;
            }
            String sql = "DELETE FROM reservation WHERE reservation_id =" + reservationId;

            try (Statement statement = connection.createStatement()) {
                int affectedrows = statement.executeUpdate(sql);
                if (affectedrows > 0) {
                    System.out.println("Reservation delete successfully");
                } else {
                    System.out.println("Reservation delete failed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean reservationExists(Connection connection, int reservationId) {
        try {
            String sql = "SELECT reservation_id FROM reservation WHERE reservation_id = " + reservationId;
            try (Statement statement = connection.createStatement();
                 ResultSet resultset = statement.executeQuery(sql)) {
                return resultset.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while (i!= 0) {
            System.out.print(".");
            Thread.sleep(500);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For using Hotel Reservation System!!!");
    }
}