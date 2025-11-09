package app;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Interactive console menu:
 * 0 = Exit
 * 1 = Add a new student
 * 2 = Update a student's email
 * 3 = Delete a student
 * 4 = View students
 */
public class MainApp {

    public static void main(String[] args) {
        Options dao = new Options();  // formerly StudentDAO
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Students Console ===");
        while (true) {
            // Always show current data first
            printAll(dao);

            // Menu
            System.out.println("\nChoose an option:");
            System.out.println("0. Exit");
            System.out.println("1. Add new student");
            System.out.println("2. Update student email");
            System.out.println("3. Delete student");
            System.out.println("4. View students");
            System.out.print("Enter choice (0-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "0":
                    System.out.println("Goodbye!");
                    return;

                case "1":
                    doAdd(scanner, dao);
                    break;

                case "2":
                    doUpdate(scanner, dao);
                    break;

                case "3":
                    doDelete(scanner, dao);
                    break;

                case "4":
                    try {
                        dao.viewAllStudents();
                    } catch (SQLException e) {
                        System.out.println("View failed: " + e.getMessage());
                    }
                    break;

                default:
                    System.out.println("Invalid choice. Please enter 0, 1, 2, 3, or 4.");
            }
        }
    }

    /** Display all students using the DAO's pretty printer. */
    private static void printAll(Options dao) {
        try {
            dao.viewAllStudents();
        } catch (SQLException e) {
            System.err.println("Failed to fetch students: " + e.getMessage());
        }
    }

    /** Add a new student by prompting fields. */
    private static void doAdd(Scanner scanner, Options dao) {
        try {
            System.out.print("First name: ");
            String first = readNonEmpty(scanner);

            System.out.print("Last name: ");
            String last = readNonEmpty(scanner);

            System.out.print("Email (must be unique): ");
            String email = readNonEmpty(scanner);

            System.out.print("Enrollment date (YYYY-MM-DD, blank for today): ");
            String dateStr = scanner.nextLine().trim();
            Date enrollDate = dateStr.isEmpty()
                    ? Date.valueOf(LocalDate.now())
                    : Date.valueOf(LocalDate.parse(dateStr)); // throws if malformed

            Student s = new Student(first, last, email, enrollDate);
            int id = dao.addStudent(s);
            System.out.println("Inserted student with ID: " + id);

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        } catch (SQLException e) {
            // 23505 is Postgres unique_violation (e.g., duplicate email)
            if ("23505".equals(e.getSQLState())) {
                System.out.println("Insert failed: email must be unique.");
            } else {
                System.out.println("Insert failed: " + e.getMessage());
            }
        }
    }

    /** Update a student's email by ID — shows table first and validates ID. */
    private static void doUpdate(Scanner scanner, Options dao) {
        try {
            // Show current students before prompting
            dao.viewAllStudents();

            int id;
            while (true) {
                System.out.print("Student ID to update: ");
                id = readInt(scanner);
                if (dao.existsStudentId(id)) break;
                System.out.println("No student found with ID " + id + ". Try again.");
            }

            System.out.print("New email: ");
            String email = readNonEmpty(scanner);

            int rows = dao.updateStudentEmail(id, email);
            if (rows == 0) {
                System.out.println("Nothing updated.");
            } else {
                System.out.println("Updated email for student " + id + ".");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Please enter a valid integer ID.");
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.out.println("Update failed: email must be unique.");
            } else {
                System.out.println("Update failed: " + e.getMessage());
            }
        }
    }

    /** Delete a student by ID — shows table first, validates, and confirms. */
    private static void doDelete(Scanner scanner, Options dao) {
        try {
            // Show current students before prompting
            dao.viewAllStudents();

            int id;
            while (true) {
                System.out.print("Student ID to delete: ");
                id = readInt(scanner);
                if (dao.existsStudentId(id)) break;
                System.out.println("No student found with ID " + id + ". Try again.");
            }

            // Optional: show the row and ask for confirmation
            Student s = dao.getStudentById(id);
            System.out.printf("Delete: [%d] %s %s <%s>? (y/N): ",
                    s.getStudentId(), s.getFirstName(), s.getLastName(), s.getEmail());
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y")) {
                System.out.println("Cancelled.");
                return;
            }

            int rows = dao.deleteStudent(id);
            if (rows == 0) {
                System.out.println("Nothing deleted.");
            } else {
                System.out.println("Deleted student " + id + ".");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Please enter a valid integer ID.");
        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }

    // ---- small input helpers ----

    private static String readNonEmpty(Scanner scanner) {
        while (true) {
            String s = scanner.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.print("Value cannot be empty. Try again: ");
        }
    }

    private static int readInt(Scanner scanner) {
        String s = scanner.nextLine().trim();
        return Integer.parseInt(s); // let caller catch NumberFormatException
    }
}
