import java.io.*;
import java.net.*;
import java.sql.*;

public class Server{
    private static final int PORT = 12345;
    public static final String DB_URL = "jdbc:sqlite:db.sqlite";
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            setupDatabase();

        } catch (IOException ex) {
            System.err.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    private static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String lecturers = """
                    CREATE TABLE IF NOT EXISTS "Lecturers" (
                    	"username"	TEXT,
                    	"password"	TEXT,
                    	"faculty"	TEXT,
                    	PRIMARY KEY("username")
                    )
                """;
            String courseInfo = """
                    CREATE TABLE IF NOT EXISTS "CourseInfo" (
                    	"coursecode"	TEXT,
                    	"coursename"	TEXT,
                    	"evaluationcriteria"	TEXT,
                    	PRIMARY KEY("coursecode")
                    )
                    """;
            String students = """
                    CREATE TABLE IF NOT EXISTS "Students" (
                    	"studentID"	TEXT,
                    	"name"	TEXT,
                    	PRIMARY KEY("studentID")
                    )
                    """;
            String courses = """
                    CREATE TABLE IF NOT EXISTS "Courses" (
                    	"lecturername"	TEXT,
                    	"coursename"	TEXT,
                    	"section"	TEXT,
                    	FOREIGN KEY("coursename") REFERENCES "CourseInfo"("coursecode"),
                    	FOREIGN KEY("lecturername") REFERENCES "Lecturers"("username")
                    )
                    """;
            String LOs = """
                    CREATE TABLE IF NOT EXISTS "LOs" (
                    	"coursecode"	TEXT,
                    	"number"	INTEGER,
                    	"text"	TEXT,
                    	FOREIGN KEY("coursecode") REFERENCES "CourseInfo"("coursecode")
                    )
                    """;
            String questions = """
                    CREATE TABLE IF NOT EXISTS "Questions" (
                    	"coursecode"	TEXT,
                    	"question"	TEXT,
                    	"answer"	TEXT,
                    	"possiblepoint"	INTEGER DEFAULT 0,
                    	"LO"	TEXT,
                    	"questionID"	TEXT,
                    	"examID"	TEXT,
                    	PRIMARY KEY("questionID"),
                    	FOREIGN KEY("coursecode") REFERENCES "CourseInfo"("coursecode"),
                    	FOREIGN KEY("examID") REFERENCES "Exams"("examID")
                    )
                    """;
            String exams = """
                    CREATE TABLE IF NOT EXISTS "Exams" (
                    	"coursecode"	TEXT,
                    	"examtype"	TEXT,
                    	"examID"	TEXT,
                    	PRIMARY KEY("examID"),
                    	FOREIGN KEY("coursecode") REFERENCES "CourseInfo"("coursecode")
                    )
                    """;

            String enrollments = """
                    CREATE TABLE IF NOT EXISTS "Enrollments" (
                    	"studentID"	TEXT,
                    	"coursecode"	TEXT,
                    	"section"	TEXT,
                    	FOREIGN KEY("coursecode") REFERENCES "CourseInfo"("coursecode"),
                    	FOREIGN KEY("studentID") REFERENCES "Students"("studentID")
                    )
                    """;

            String grades = """
                    CREATE TABLE IF NOT EXISTS "Grades" (
                    	"studentID"	TEXT,
                    	"questionID"	TEXT,
                    	"point"	INTEGER,
                    	FOREIGN KEY("questionID") REFERENCES "Questions"("questionID"),
                    	FOREIGN KEY("studentID") REFERENCES "Students"("studentID")
                    )
                    """;
            stmt.executeUpdate(lecturers); stmt.executeUpdate(courseInfo); stmt.executeUpdate(students);
            stmt.executeUpdate(courses); stmt.executeUpdate(LOs); stmt.executeUpdate(questions);
            stmt.executeUpdate(exams); stmt.executeUpdate(enrollments); stmt.executeUpdate(grades);
        } catch (SQLException ex) {
            System.err.println("Database setup error: " + ex.getMessage());
        }
    }
}