package org.example.files.db.entities.implementation;

import org.example.dao.StudentDao;
import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.InsertStatement;
import org.example.files.db.entities.Entity;
import org.example.pojo.Student;

import java.sql.*;

public class StudentEntity implements Entity {
    @Override
    public void createIfNotExist(Statement statement) throws SQLException {
        String query = new CreateTableQueryBuilder("current.students")
                .addKey("roll_no", "string", true)
                .addKey("name", "string")
                .addKey("sem", "integer")
                .addKey("sec", "integer")
                .addKey("email", "string")
                .addKey("attendance", "integer")
                .addKey(new CreateTableQueryBuilder
                        .KeyEntry("phone_no", "string")
                        .notNull(false)
                )
                .addKey(new CreateTableQueryBuilder
                        .KeyEntry("address", "string")
                        .notNull(false)
                )
                .build();
        statement.execute(query);
    }

    @Override
    public void clearMemory() {
        StudentDao.getInstance().clear();
    }

    @Override
    public void clearDataFromCurrent(Statement statement) throws SQLException {
        statement.execute("DELETE FROM current.students");
    }

    @Override
    public void loadFromCurrent(Statement statement) throws SQLException {
        StudentDao studentDao = StudentDao.getInstance();

        try (ResultSet results = statement.executeQuery("SELECT * FROM current.students")) {
            while (results.next()) {
                String roll = results.getString("roll_no");
                String name = results.getString("name");
                int sem = results.getInt("sem");
                int sec = results.getInt("sec");
                String email = results.getString("email");
                int attendance = results.getInt("attendance");
                String phoneNo = results.getString("phone_no");
                String address = results.getString("address");
                Student student = new Student(name, roll, sem, sec, email, attendance, phoneNo, address);
                studentDao.put(roll, student);
            }
        }
    }

    @Override
    public void saveToCurrent(Connection connection) throws SQLException {
        for (Student student: StudentDao.getInstance().values()) {
            new InsertStatement(connection, "current.students")
                    .add(student.getRollNo())
                    .add(student.getName())
                    .add(student.getSem())
                    .add(student.getSec())
                    .add(student.getEmail())
                    .add(student.getAttendance())
                    .add(student.getPhoneNumber())
                    .add(student.getAddress())
                    .executeInsert();
        }
    }
}
