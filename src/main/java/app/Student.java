package app;

import java.sql.Date;


public class Student {
    private Integer studentId;
    private String firstName;
    private String lastName;
    private String email;
    private Date enrollmentDate;

    public Student(Integer studentId, String firstName, String lastName, String email, Date enrollmentDate) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.enrollmentDate = enrollmentDate;
    }

    public Student(String firstName, String lastName, String email, Date enrollmentDate) {
        this(null, firstName, lastName, email, enrollmentDate);
    }

    public Integer getStudentId() { return studentId; }
    public String  getFirstName() { return firstName; }
    public String  getLastName()  { return lastName; }
    public String  getEmail()     { return email; }
    public Date    getEnrollmentDate() { return enrollmentDate; }

    public void setStudentId(Integer id) { this.studentId = id; }
    public void setEmail(String email)   { this.email = email; }

    @Override
    public String toString() {
        return String.format("Student{id=%s, first='%s', last='%s', email='%s', enrolled=%s}",
                studentId, firstName, lastName, email, enrollmentDate);
    }
}
