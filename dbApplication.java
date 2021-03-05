import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import oracle.jdbc.driver.*;
import oracle.sql.*;

public class dbApplication {
    public static String user;
    public static boolean again = true;

    public static void main(String args[]) throws SQLException, IOException {
        String username, password;
        username = "\"18046521d\""; // Your Oracle Account ID
        password = "Iamthomas35"; // Password of Oracle Account

        // Connection
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        OracleConnection conn = (OracleConnection) DriverManager
                .getConnection("jdbc:oracle:thin:@studora.comp.polyu.edu.hk:1521:dbms", username, password);

        login(conn);
        while (again) {
            try {
                welcome(conn);
            } catch (Exception e) {
                System.out.println("\nInvalid Command or Syntax\n");
            }
        }
        
        // close connection
        conn.close();
    }

    public static void login(OracleConnection conn) throws SQLException, IOException {
        while (true) {
            Scanner input = new Scanner(System.in);
            System.out.print("Please enter your Username: ");
            String username = input.nextLine();

            if (username.equals("admin")) {
                System.out.println("\nYou're now logged in as an Administrator.\n");
                user = "admin";
                return;
            }

            Statement stmt = conn.createStatement();
            ResultSet rset = stmt.executeQuery("SELECT STUDENT_ID FROM STUDENTS");
            while (rset != null && rset.next()) {
                String id = rset.getString("STUDENT_ID");
                if (id.equals(username)) {
                    System.out.println("\nYou're now logged in as a Student.\n");
                    user = username;
                    return;
                }
            }

            System.out.println("\nThe username that you've entered is invalid. Please try again!\n");
        }
    }

    public static void welcome(OracleConnection conn) throws SQLException, IOException {
        Scanner input = new Scanner(System.in);
        String cmd;
        System.out.println("-----------------------------------------");
        System.out.println("Type the category that you want to access: ");
        if (user.equals("admin")) {
            System.out.print("[1]-Courses \n[2]-Students \n[3]-Enrollment  \n[0]-Exit  \nPlease enter your choice: ");
            cmd = input.nextLine();
            switch(cmd) {
                case "1": opCoursesAdmin(conn); break;
                case "2": opStudentsAdmin(conn); break;
                case "3": opEnrollmentAdmin(conn); break;
                case "0": again = false; return;
                default: System.out.println("Invalid Command!");
            }
        }
        else {
            System.out.print("[1]-Courses \n[2]-Personal Inforamtion \n[0]-Exit \nPlease enter your choice: ");
            cmd = input.nextLine();
            switch(cmd) {
                case "1": opCoursesStudent(conn); break;
                case "2": opStudentsStudent(conn); break;
                case "0": again = false; return;
                default: System.out.println("Invalid Command!");
            }
        }
        System.out.println("-----------------------------------------");
    }

    public static void opCoursesStudent(OracleConnection conn) throws SQLException, IOException {
        while (true) {
            Statement stmt = conn.createStatement();
            ResultSet rset = null;

            System.out.println("-----------------------------------------");
            System.out.print("[1]-View all courses \n" + "[2]-Show the registered courses \n"
                    + "[3]-Course Registeration \n" + "[0]-Back \n" + "Please enter your choice: ");
            Scanner input = new Scanner(System.in);
            String cmd = input.nextLine();
            System.out.println("-----------------------------------------");

            switch (cmd) {
            case "1":
                rset = stmt.executeQuery("SELECT * FROM COURSES");
                break;
            case "2":
                rset = stmt.executeQuery("SELECT * FROM COURSES WHERE COURSE_ID IN " + "(SELECT COURSE_ID FROM ENROLLMENT "
                        + "WHERE STUDENT_ID = \'" + user + "\')");
                break;
            case "3":
                System.out.print("Please enter the Course ID that you want to register: ");
                String course_id = input.nextLine();
                java.util.Date date = new java.util.Date();
                SimpleDateFormat dFormat = new SimpleDateFormat("yyyy/MM/dd");
                stmt.executeQuery("INSERT INTO ENROLLMENT VALUES(" + "\'" + user + "\'," + "\'" + course_id + "\',"
                        + "TO_DATE(\'" + dFormat.format(date) + "\', \'YYYY/MM/DD\'), 0)");
                System.out.println("Success!");
                break;
            case "0":
                return;
            default:
                System.out.println("\nInvalid Operation!\n");
            }
            
            if (rset != null) {
                ResultSetMetaData rsmd = rset.getMetaData();
                System.out.printf("%-10s%-40s%-20s%-3s\n", rsmd.getColumnName(1), rsmd.getColumnName(2), rsmd.getColumnName(3), rsmd.getColumnName(4));
            }

            while (rset != null && rset.next()) {
                String id = rset.getString(1);
                String title = rset.getString(2);
                String staff = rset.getString(3);
                String section = rset.getString(4);
    
                System.out.printf("%-10s%-40s%-20s%-3s\n", id, title, staff, section);
            }    
        }
    }

    public static void opStudentsStudent(OracleConnection conn) throws SQLException, IOException {
        while (true) {
            Statement stmt = conn.createStatement();
            ResultSet rset = null;
            System.out.println("-----------------------------------------");
            System.out.print("[1]-View Personal Information \n" + "[2]-Modify Personal Information\n" + "[0]-Back\n" + "Please enter your choice: ");
            Scanner input = new Scanner(System.in);
            String cmd = input.nextLine();
            System.out.println("-----------------------------------------");
            switch (cmd) {
            case "1":
                rset = stmt.executeQuery("SELECT * FROM STUDENTS WHERE STUDENT_ID = \'" + user + "\'");
                break;
            case "2":
                System.out.print("Name: ");
                String name = input.nextLine();
                System.out.print("Date of Birth: (yyyy/mm/dd) ");
                String dob = input.nextLine();
                System.out.print("Gender: (0-Female, 1-Male) ");
                String gender = input.nextLine().equals("0") ? "FEMALE" : "MALE";
                stmt.execute("UPDATE STUDENTS SET STUDENT_NAME = \'" + name + "\'," + "BIRTHDATE = " + "TO_DATE(" +"\'" + dob + "\'" + ", 'YYYY/MM/DD'), "
                        + "GENDER = \'" + gender + "\'" + "WHERE STUDENT_ID = \'" + user + "\'");
                System.out.println("Success!");
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid Command!");
            }
            
            while (rset != null && rset.next()) {
                String id = rset.getString(1);
                String name = rset.getString(2);
                String dept = rset.getString(3);
                String address = rset.getString(4);
                Date dob = rset.getDate(5);
                String gender = rset.getString(6);
                System.out.printf("Student ID: %s\n" + "Name: %s\n" + "Department: %s\n" + "Address: %s\n"
                        + "Date of Birth: %s\n" + "Gender: %s\n", id, name, dept, address, dob, gender);
            }    
        }
    }

    public static void opCoursesAdmin(OracleConnection conn) throws SQLException, IOException {
        while (true) {
            Statement stmt = conn.createStatement();
            ResultSet rset = null;
            
            System.out.println("-----------------------------------------");
            System.out.print("[1]-View all courses \n" + 
                            "[2]-Add a new course \n" +
                            "[3]-Modify the course \n" +
                            "[4]-Delete a course \n" +
                            "[0]-Back \n" +
							"Please enter your choice: ");
            Scanner input = new Scanner(System.in);
            String cmd = input.nextLine();
            System.out.println("-----------------------------------------");
            switch (cmd) {
            case "1":
                rset = stmt.executeQuery("SELECT * FROM COURSES");
                break;
            case "2":
                System.out.print("CourseID: ");
                String id = input.nextLine();
                System.out.print("Course Name: ");
                String name = input.nextLine();
                System.out.print("Staff name: ");
                String staff = input.nextLine();
                System.out.print("Section: ");
                String section = input.nextLine();
    
                stmt.executeQuery("INSERT INTO COURSES VALUES(" + "\'" + id + "\'," + "\'" + name + "\'," + "\'" + staff
                        + "\'," + "\'" + section + "\'" + ")");
                System.out.println("Success!");
                break;
            case "3":
                System.out.print("The required CourseID: ");
                id = "\'"+input.nextLine()+"\'";
                System.out.print("Course Name: ");
                name = "\'"+input.nextLine()+"\'";
                System.out.print("Staff name: ");
                staff = "\'"+input.nextLine()+"\'";
                System.out.print("Section: ");
                section = "\'"+input.nextLine()+"\'";
    
                stmt.executeQuery("UPDATE COURSES SET COURSE_TITLE =" + name 
                                +", STAFF_NAME = "+ staff
                                +", SECTION =  " + section
                                +" WHERE COURSE_ID =" + id);
                System.out.println("Success!");
                break;
            case "4":
                System.out.print("The required course: ");
                id = "'" + input.nextLine() +"'";
                stmt.executeQuery("DELETE FROM COURSES WHERE COURSE_ID = " + id);
                System.out.println("Success!");
                break;
            case "0":
                return;
            default:
                System.out.println("\nInvalid Operation!\n");
            }
            
            if (rset != null) {
                ResultSetMetaData rsmd = rset.getMetaData();
                System.out.printf("%-10s%-40s%-20s%-3s\n", rsmd.getColumnName(1), rsmd.getColumnName(2), rsmd.getColumnName(3), rsmd.getColumnName(4));
            }
            while (rset != null && rset.next()) {
                String id = rset.getString(1);
                String title = rset.getString(2);
                String staff = rset.getString(3);
                String section = rset.getString(4);
    
                System.out.printf("%-10s%-40s%-20s%-3s\n", id, title, staff, section);
            }    
        }
    }

    public static void opStudentsAdmin(OracleConnection conn) throws SQLException, IOException {
        while (true) {
            Statement stmt = conn.createStatement();
            ResultSet rset = null;
            System.out.println("-----------------------------------------");
            System.out.print("[1]-View all students \n" + 
                            "[2]-Add a new student \n" +
                            "[3]-Modify the student \n" +
                            "[4]-Delete a student \n" +
                            "[5]-Search by department \n" +
                            "[0]-Back \n" +
							"Please enter your choice: ");
            Scanner input = new Scanner(System.in);
            String cmd = input.nextLine();
            System.out.println("-----------------------------------------");

            switch (cmd) {
            case "1":
                rset = stmt.executeQuery("SELECT * FROM STUDENTS");
                break;
            case "2":
                System.out.print("Student id: ");
                String id = input.nextLine();
                System.out.print("Student Name: ");
                String name = input.nextLine();
                System.out.print("Department: ");
                String dept = input.nextLine();
                System.out.print("Address: ");
                String address = input.nextLine();
                System.out.print("Date of Birth: (yyyy/mm/dd) ");
                String dob = input.nextLine();
                System.out.print("Gender: (0-Female, 1-Male)");
                String gender = (input.nextLine().equals("0"))? "FEMALE": "MALE";

                stmt.executeQuery("INSERT INTO STUDENTS VALUES(" + 
                                "\'" + id + "\'," +
                                "\'" + name + "\'," +
                                "\'" + dept + "\'," +
                                "\'" + address + "\'," +
                                "TO_DATE(" +"\'" + dob + "\'" + ", 'YYYY/MM/DD'), " +
                                "\'" + gender + "\')");
                System.out.println("Success!");
                break;
            case "3":
                System.out.print("The required StudentID: ");
                id = "\'"+input.nextLine()+"\'";
                System.out.print("New Name: ");
                name = "\'"+input.nextLine()+"\'";
                System.out.print("New department name: ");
                dept = "\'"+input.nextLine()+"\'";
                System.out.print("New address: ");
                address = "\'"+input.nextLine()+"\'";
                System.out.print("New Date of Birth: (yyyy/mm/dd) ");
                dob = "\'"+input.nextLine()+"\'";
                System.out.print("New Gender: (0-Female, 1-Male)");
                gender = input.nextLine().equals("0")? "FEMALE": "MALE";

                stmt.executeQuery("UPDATE STUDENTS SET STUDENT_NAME =" + name 
                                +", DEPARTMENT = "+ dept
                                +", ADDRESS =  " + address
                                +", BIRTHDATE = " + "TO_DATE(" + dob + ", 'YYYY/MM/DD')"
                                +", GENDER = " + "\'"+gender+"\'"
                                +" WHERE STUDENT_ID =" + id);
                System.out.println("Success!");
                break;
            case "4":
                System.out.print("The required Student ID: ");
                id = "\'" + input.nextLine() +"\'";
                stmt.executeQuery("DELETE FROM STUDENTS WHERE STUDENT_ID = " + id);
                System.out.println("Success!");
                break;
            case "5":
                System.out.print("Department name: ");
                dept = "\'%"+input.nextLine()+"%\'";
                rset = stmt.executeQuery("SELECT * FROM STUDENTS WHERE DEPARTMENT LIKE " + dept);
                break;
            case "0":
                return;
            default:
                System.out.println("\nInvalid Operation!\n");
            }

            if (rset != null) {
                ResultSetMetaData rsmd = rset.getMetaData();
                System.out.printf("%-12s%-32s%-12s%-70s%-12s%-8s\n", rsmd.getColumnName(1), rsmd.getColumnName(2), rsmd.getColumnName(3), rsmd.getColumnName(4), rsmd.getColumnName(5), rsmd.getColumnName(6));
            }
            while (rset != null && rset.next()) {
                String id = rset.getString(1);
                String name = rset.getString(2);
                String dept = rset.getString(3);
                String address = rset.getString(4);
                Date dob = rset.getDate(5);
                String gender = rset.getString(6);

                System.out.printf("%-12s%-32s%-12s%-70s%-12s%-8s\n", id, name, dept, address, dob, gender);
            }
        }
    }

    public static void opEnrollmentAdmin(OracleConnection conn) throws SQLException, IOException {
        while (true) {
            Statement stmt = conn.createStatement();
            ResultSet rset = null;

            System.out.print("[1]-Modify student's grade \n" + 
                            "[0]-Back \n" +
							"Please enter your choice: ");
            Scanner input = new Scanner(System.in);
            String cmd = input.nextLine();
            switch (cmd) {
                case "1":
                    System.out.print("Student ID: ");
                    String student_id = "\'" + input.nextLine() + "\'";
                    System.out.print("Course ID: ");
                    String course_id = "\'" + input.nextLine() + "\'";
                    System.out.print("New Grade: ");
                    String grade = input.nextLine();
                    stmt.executeQuery("UPDATE ENROLLMENT SET GRADE = " + Integer.parseInt(grade) +
                                     " WHERE COURSE_ID = " + course_id + 
                                     "AND STUDENT_ID = " + student_id);
                    System.out.println("Success!");
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid Command!");
            }
        }
    }
}
