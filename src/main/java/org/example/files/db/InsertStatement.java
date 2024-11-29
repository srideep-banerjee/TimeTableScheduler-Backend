package org.example.files.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class InsertStatement {
    private final StringBuilder queryBuilder;
    private final ArrayList<String> addedStringValues = new ArrayList<>();
    private final Connection connection;

    public InsertStatement(Connection connection, String tableName) {
        this.connection = connection;
        this.queryBuilder = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
    }

    public InsertStatement(Connection connection, String tableName, boolean orIgnore) {
        this.connection = connection;
        this.queryBuilder = new StringBuilder("INSERT ");
        if (orIgnore) {
            queryBuilder.append("OR IGNORE ");
        }
        queryBuilder.append("INTO ")
                .append(tableName)
                .append(" VALUES (");
    }

    public InsertStatement add(String value) {
        queryBuilder.append("?, ");
        addedStringValues.add(value);
        return this;
    }

    public InsertStatement add(int value) {
        queryBuilder.append(value).append(", ");
        return this;
    }

    public InsertStatement add(boolean value) {
        queryBuilder.append(value ? 1 : 0).append(", ");
        return this;
    }

    public void executeInsert() throws SQLException {
        queryBuilder.setCharAt(queryBuilder.length() - 2, ')');
        try (PreparedStatement preparedStatement = connection
                .prepareStatement(queryBuilder.toString())) {
            for (int i = 0; i < addedStringValues.size(); i++)
                preparedStatement.setString(i + 1, addedStringValues.get(i));
            preparedStatement.executeUpdate();
        }
    }
}
