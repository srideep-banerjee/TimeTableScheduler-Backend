package org.example.files.db;

import java.sql.*;

public class ConfigHandler {
    private final Connection connection;

    public ConfigHandler(Connection connection) {
        this.connection = connection;
    }

    public String getGlobal(String key) throws SQLException {
        String query = "SELECT data FROM config WHERE name = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return null;
                return resultSet.getString(1);
            }
        }
    }

    public String getLocal(String key) throws SQLException {
        String query = "SELECT data FROM current.config WHERE name = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next())
                    return null;
                return resultSet.getString(1);
            }
        }
    }

    public void putGlobal(String key, String value) throws SQLException {
        new InsertStatement(connection, "config", InsertStatement.OnConflict.REPLACE)
                .add(key)
                .add(value)
                .executeInsert();
    }

    public void putLocal(String key, String value) throws SQLException {
        new InsertStatement(connection, "current.config", InsertStatement.OnConflict.REPLACE)
                .add(key)
                .add(value)
                .executeInsert();
    }

    public void deleteGlobal(String key) throws SQLException {
        String query = "DELETE FROM config WHERE name = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, key);
            statement.executeUpdate();
        }
    }

    public void deleteLocal(String key) throws SQLException {
        String query = "DELETE FROM current.config WHERE name = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, key);
            statement.executeUpdate();
        }
    }
}
