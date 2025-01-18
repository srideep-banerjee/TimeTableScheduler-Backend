package org.example.files.db.entities;

import org.example.dao.SubjectDao;
import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.InsertStatement;
import org.example.pojo.Subject;

import java.sql.*;
import java.util.ArrayList;

import static org.example.files.db.entities.CommonData.subjectCodeForeignKey;

public class SubjectEntity implements Entity {
    @Override
    public void createIfNotExist(Statement statement) throws SQLException {
        String query = new CreateTableQueryBuilder("current.subjects")
                .addKey("subject_code", "string", true)
                .addKey("sem", "integer")
                .addKey("lecture_count", "integer")
                .addKey("is_practical", "integer")
                .addKey("is_free", "integer")
                .build();
        statement.execute(query);
        query = new CreateTableQueryBuilder("current.subject_room_codes")
                .addKey("subject_code", "string")
                .addKey("room_code", "string")
                .primaryKeys("subject_code", "room_code")
                .addForeignKey(subjectCodeForeignKey)
                .build();
        statement.execute(query);
    }

    @Override
    public void clearMemory() {
        SubjectDao.getInstance().clear();
    }

    @Override
    public void clearDataFromCurrent(Statement statement) throws SQLException {
        statement.execute("DELETE FROM current.subjects");
        statement.execute("DELETE FROM current.subject_room_codes");
    }

    @Override
    public void loadFromCurrent(Statement statement) throws SQLException {
        Connection connection = statement.getConnection();

        try (ResultSet results = statement.executeQuery("SELECT * FROM current.subjects")) {
            while (results.next()) {
                String code = results.getString("subject_code");
                int sem = results.getInt("sem");
                int lectureCount = results.getInt("lecture_count");
                boolean isPractical = results.getInt("is_practical") == 1;
                boolean isFree = results.getInt("is_free") == 1;
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT room_code FROM current.subject_room_codes WHERE subject_code = ?");
                preparedStatement.setString(1, code);
                ArrayList<String> roomCodes;
                try (ResultSet roomResults = preparedStatement.executeQuery()) {
                    roomCodes = new ArrayList<>();
                    while (roomResults.next()) {
                        roomCodes.add(roomResults.getString(1));
                    }
                }
                Subject subject = new Subject(sem, lectureCount, isPractical, roomCodes, isFree);
                SubjectDao.getInstance().put(code, subject);
            }
        }
    }

    @Override
    public void saveToCurrent(Connection connection) throws SQLException {

        for (String subjectCode: SubjectDao.getInstance().keySet()) {
            Subject subject = SubjectDao.getInstance().get(subjectCode);
            new InsertStatement(connection, "current.subjects")
                    .add(subjectCode)
                    .add(subject.getSem())
                    .add(subject.getLectureCount())
                    .add(subject.isPractical())
                    .add(subject.isFree())
                    .executeInsert();
            for (String roomCode: subject.getRoomCodes()) {
                new InsertStatement(connection, "current.subject_room_codes")
                        .add(subjectCode)
                        .add(roomCode)
                        .executeInsert();
            }
        }
    }
}
