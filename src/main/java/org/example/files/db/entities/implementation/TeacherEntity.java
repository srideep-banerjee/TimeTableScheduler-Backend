package org.example.files.db.entities.implementation;

import org.example.algorithms.DayPeriod;
import org.example.dao.TeacherDao;
import org.example.files.TTSFileException;
import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.InsertStatement;
import org.example.files.db.entities.Entity;
import org.example.pojo.Teacher;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.example.files.db.entities.CommonData.subjectCodeForeignKey;
import static org.example.files.db.entities.CommonData.teacherNameForeignKey;

public class TeacherEntity implements Entity {

    @Override
    public void createIfNotExist(Statement statement) throws SQLException {

        String query = new CreateTableQueryBuilder("current.teachers")
                .addKey("teacher_name", "string", true)
                .addKey("always_free", "integer")
                .build();
        statement.execute(query);
        query = new CreateTableQueryBuilder("current.teacher_known_subjects")
                .addKey("teacher_name", "string")
                .addKey("subject_code", "string")
                .primaryKeys("teacher_name", "subject_code")
                .addForeignKey(teacherNameForeignKey)
                .addForeignKey(subjectCodeForeignKey)
                .build();
        statement.execute(query);
        query = new CreateTableQueryBuilder("current.teacher_free")
                .addKey("teacher_name", "string")
                .addKey("day_period", "integer")
                .primaryKeys("teacher_name", "day_period")
                .addForeignKey(teacherNameForeignKey)
                .build();
        statement.execute(query);
    }

    @Override
    public void clearMemory() {
        TeacherDao.getInstance().clear();
    }

    @Override
    public void clearDataFromCurrent(Statement statement) throws SQLException {
        statement.execute("DELETE FROM current.teachers");
        statement.execute("DELETE FROM current.teacher_known_subjects");
        statement.execute("DELETE FROM current.teacher_free");
    }

    @Override
    public void loadFromCurrent(Statement statement) throws SQLException {
        Connection connection = statement.getConnection();

        try (ResultSet results = statement.executeQuery("SELECT * FROM current.teachers")) {
            while (results.next()) {
                String teacherName = results.getString("teacher_name");
                boolean alwaysFree = results.getInt("always_free") == 1;
                HashSet<List<Integer>> freeTime = new HashSet<>();
                if (!alwaysFree) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT day_period FROM current.teacher_free WHERE teacher_name = ?")) {
                        preparedStatement.setString(1, teacherName);
                        try (ResultSet teacherArrayResults = preparedStatement.executeQuery()) {
                            while (teacherArrayResults.next()) {
                                DayPeriod dayPeriod = new DayPeriod(teacherArrayResults.getShort("day_period"));
                                freeTime.add(Arrays.asList((int) dayPeriod.day, (int) dayPeriod.period));
                            }
                        }
                    }
                }
                HashSet<String> knownSubjects = new HashSet<>();
                try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT subject_code FROM current.teacher_known_subjects WHERE teacher_name = ?")) {
                    preparedStatement.setString(1, teacherName);
                    try (ResultSet teacherArrayResults = preparedStatement.executeQuery()) {
                        while (teacherArrayResults.next())
                            knownSubjects.add(teacherArrayResults.getString("subject_code"));
                    }
                }
                TeacherDao.getInstance().put(teacherName, new Teacher(freeTime, knownSubjects));
            }
        }
    }

    @Override
    public void saveToCurrent(Connection connection) throws SQLException {

        for (String teacherName: TeacherDao.getInstance().keySet()) {
            Teacher teacher = TeacherDao.getInstance().get(teacherName);
            new InsertStatement(connection, "current.teachers")
                    .add(teacherName)
                    .add(teacher.getFreeTime().isEmpty())
                    .executeInsert();
            for (String subjectCode: teacher.getSubjects()) {
                try {
                    new InsertStatement(connection, "current.teacher_known_subjects")
                            .add(teacherName)
                            .add(subjectCode)
                            .executeInsert();
                } catch (InsertStatement.ForeignKeyException e) {
                    if (e.getColumnName().equals("subject_code")) {
                        String msg = "'" +
                                subjectCode +
                                "' was added as known subject for '" +
                                teacherName +
                                "'" +
                                " but doesn't exist in subjects";
                        throw new TTSFileException(msg);
                    }
                }
            }
            for (List<Integer> freeDayPeriod: teacher.getFreeTime()) {
                byte day = freeDayPeriod.get(0).byteValue();
                byte period = freeDayPeriod.get(1).byteValue();
                new InsertStatement(connection, "current.teacher_free")
                        .add(teacherName)
                        .add(new DayPeriod(day, period).getCompact())
                        .executeInsert();
            }
        }
    }
}
