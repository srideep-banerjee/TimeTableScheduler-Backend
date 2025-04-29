package org.example.files.db.entities.implementation;

import org.example.dao.ConfigDao;
import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.InsertStatement;
import org.example.files.db.entities.Entity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class ConfigEntity implements Entity {
    @Override
    public void createIfNotExist(Statement statement) throws SQLException {
        String query = new CreateTableQueryBuilder("current.config")
                .addKey("name", "string", true)
                .addKey("data", "string")
                .build();
        statement.execute(query);
    }

    @Override
    public void clearMemory() {
        ConfigDao.getInstance().clear();
    }

    @Override
    public void clearDataFromCurrent(Statement statement) throws SQLException {
        statement.execute("DELETE FROM current.config");
    }

    @Override
    public void loadFromCurrent(Statement statement) throws SQLException {
        ConfigDao configDao = ConfigDao.getInstance();

        try (ResultSet resultSet = statement.executeQuery("SELECT * FROM current.config")) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String data = resultSet.getString("data");

                configDao.put(name, data);
            }
        }
    }

    @Override
    public void saveToCurrent(Connection connection) throws SQLException {
        for (Map.Entry<String, String> entry: ConfigDao.getInstance().entrySet()) {
            new InsertStatement(connection, "current.config")
                    .add(entry.getKey())
                    .add(entry.getValue())
                    .executeInsert();
        }
    }
}
