package org.example.files.db.entities.implementation;

import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.entities.Entity;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
    public void clearMemory() {}

    @Override
    public void clearDataFromCurrent(Statement statement) throws SQLException {
        statement.execute("DELETE FROM current.config");
    }

    @Override
    public void loadFromCurrent(Statement statement) {}

    @Override
    public void saveToCurrent(Connection connection) {}
}
