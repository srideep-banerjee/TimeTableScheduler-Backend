package org.example.files.db.entities.implementation;

import org.example.algorithms.DayPeriod;
import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.InsertStatement;
import org.example.files.db.entities.Entity;
import org.example.pojo.ScheduleSolution;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.example.files.db.entities.CommonData.subjectCodeForeignKey;
import static org.example.files.db.entities.CommonData.teacherNameForeignKey;

public class ScheduleSolutionEntity implements Entity {
    @Override
    public void createIfNotExist(Statement statement) throws SQLException {
        String query = new CreateTableQueryBuilder("current.schedule_subject_details")
                .addKey("sem", "integer")
                .addKey("sec", "integer")
                .addKey("subject_code", "string")
                .addKey(new CreateTableQueryBuilder.KeyEntry("teacher_name", "string").notNull(false))
                .addKey("room_code", "string")
                .primaryKeys("sem", "sec", "subject_code")
                .addForeignKey(subjectCodeForeignKey)
                .addForeignKey(teacherNameForeignKey)
                .addForeignKey(
                        new CreateTableQueryBuilder.ForeignKey("subject_room_codes")
                                .addReference("room_code")
                                .addReference("subject_code")
                )
                .build();
        statement.execute(query);
        query = new CreateTableQueryBuilder("current.schedule_room_entries")
                .addKey("sem", "integer")
                .addKey("sec", "integer")
                .addKey("subject_code", "string")
                .addKey("room_code", "string")
                .primaryKeys("sem", "sec", "subject_code")
                .addForeignKey(subjectCodeForeignKey)
                .addForeignKey(
                        new CreateTableQueryBuilder.ForeignKey("subject_room_codes")
                                .addReference("room_code")
                                .addReference("subject_code")
                ).build();
        statement.execute(query);
        query = new CreateTableQueryBuilder("current.schedule_period_entries")
                .addKey("sem", "integer")
                .addKey("sec", "integer")
                .addKey("day_period", "integer")
                .addKey("subject_code", "string")
                .addKey(new CreateTableQueryBuilder.KeyEntry("teacher_name", "string").notNull(false))
                .primaryKeys("sem", "sec", "day_period")
                .addForeignKey(teacherNameForeignKey)
                .addForeignKey(
                        new CreateTableQueryBuilder.ForeignKey("schedule_room_entries")
                                .addReference("sem")
                                .addReference("sec")
                                .addReference("subject_code")
                ).build();
        statement.execute(query);
    }

    @Override
    public void clearMemory() {
        ScheduleSolution.getInstance().updateStructure();
        ScheduleSolution.getInstance().resetData();
    }

    @Override
    public void clearDataFromCurrent(Statement statement) throws SQLException {
        statement.execute("DELETE FROM current.schedule_room_entries");
        statement.execute("DELETE FROM current.schedule_period_entries");
    }

    @Override
    public void loadFromCurrent(Statement statement) throws SQLException {
        ScheduleSolution scheduleSolution = ScheduleSolution.getInstance();
        try (ResultSet resultSet = statement.executeQuery("SELECT * FROM current.schedule_room_entries NATURAL JOIN current.schedule_period_entries")) {
            scheduleSolution.updateStructure();
            ScheduleSolution.SolutionAccumulator accumulator = new ScheduleSolution.SolutionAccumulator();
            while (resultSet.next()) {
                int sem = resultSet.getInt("sem");
                int sec = resultSet.getInt("sec");
                DayPeriod dayPeriod = new DayPeriod(resultSet.getShort("day_period"));
                String subject = resultSet.getString("subject_code");
                String teacher = resultSet.getString("teacher_name");
                String room = resultSet.getString("room_code");
                accumulator.add(sem, sec, dayPeriod.day, dayPeriod.period, subject, teacher, room);
            }
            scheduleSolution.setData(accumulator.accumulate());
        }
    }

    @Override
    public void saveToCurrent(Connection connection) throws IOException {

        ScheduleSolution.SolutionIteratorCallback callback = (sem, sec, day, period, subject, teacher, room) -> {
            try {
                new InsertStatement(connection, "current.schedule_room_entries", InsertStatement.OnConflict.IGNORE)
                        .add(sem)
                        .add(sec)
                        .add(subject)
                        .add(room)
                        .executeInsert();
                new InsertStatement(connection, "current.schedule_period_entries")
                        .add(sem)
                        .add(sec)
                        .add(new DayPeriod((byte) day, (byte) period).getCompact())
                        .add(subject)
                        .add(teacher)
                        .executeInsert();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        try {
            new ScheduleSolution.SolutionIterator(callback).iterate();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
