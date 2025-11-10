package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Options {

    public int addStudent(Student s) throws SQLException {
        String sql = """
            INSERT INTO public.students (first_name, last_name, email, enrollment_date)
            VALUES (?, ?, ?, ?)
            RETURNING student_id
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setDate(4, s.getEnrollmentDate());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("student_id");
                    s.setStudentId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Insert failed; no ID returned.");
    }

    public List<Student> getAllStudents() throws SQLException {
        String sql = """
            SELECT student_id, first_name, last_name, email, enrollment_date
            FROM public.students
            ORDER BY student_id
            """;
        List<Student> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getDate("enrollment_date")
                ));
            }
        }
        return result;
    }

    public int updateStudentEmail(int studentId, String newEmail) throws SQLException {
        String sql = "UPDATE public.students SET email = ? WHERE student_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setInt(2, studentId);
            return ps.executeUpdate();
        }
    }

    public int deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM public.students WHERE student_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate();
        }
    }

    public Student getStudentById(int id) throws SQLException {
        String sql = """
            SELECT student_id, first_name, last_name, email, enrollment_date
            FROM public.students
            WHERE student_id = ?
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getDate("enrollment_date")
                    );
                }
            }
        }
        return null;
    }

    public boolean existsStudentId(int id) throws SQLException {
        String sql = "SELECT 1 FROM public.students WHERE student_id = ? LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // -------------------------------------------------------------------------
    // View helper: prints a formatted table of all students
    // -------------------------------------------------------------------------
    public void viewAllStudents() throws SQLException {
        var students = getAllStudents();

        String[] headers = {"ID", "First Name", "Last Name", "Email", "Enrollment Date"};
        List<String[]> rows = new ArrayList<>();
        for (Student s : students) {
            rows.add(new String[]{
                    s.getStudentId() == null ? "" : String.valueOf(s.getStudentId()),
                    nz(s.getFirstName()),
                    nz(s.getLastName()),
                    nz(s.getEmail()),
                    s.getEnrollmentDate() == null ? "" : s.getEnrollmentDate().toString()
            });
        }

        int[] w = new int[headers.length];
        for (int i = 0; i < headers.length; i++) w[i] = headers[i].length();
        for (String[] r : rows)
            for (int i = 0; i < r.length; i++)
                w[i] = Math.max(w[i], r[i].length());

        w[1] = Math.min(w[1], 18);
        w[2] = Math.min(w[2], 18);
        w[3] = Math.min(w[3], 32);

        String border = makeBorder(w);
        System.out.println();
        System.out.println(border);
        System.out.println(row(headers, w));
        System.out.println(border);
        for (String[] r : rows) {
            r[1] = ellipsize(r[1], w[1]);
            r[2] = ellipsize(r[2], w[2]);
            r[3] = ellipsize(r[3], w[3]);
            System.out.println(row(r, w));
        }
        System.out.println(border);
        System.out.printf("Total: %d student%s%n%n", students.size(), students.size() == 1 ? "" : "s");
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String ellipsize(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        if (max <= 1) return s.substring(0, max);
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static String makeBorder(int[] w) {
        StringBuilder sb = new StringBuilder("+");
        for (int x : w) sb.append("-".repeat(x + 2)).append("+");
        return sb.toString();
    }

    private static String row(String[] cells, int[] w) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < cells.length; i++) {
            String c = cells[i] == null ? "" : cells[i];
            sb.append(" ").append(padRight(c, w[i])).append(" |");
        }
        return sb.toString();
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }
}
