package org.example.files.db;

import org.example.files.TTSFileException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class InsertStatement {
    private final StringBuilder queryBuilder;
    private final ArrayList<String> addedStringValues = new ArrayList<>();
    private final Connection connection;
    private final String tableName;

    public InsertStatement(Connection connection, String tableName) {
        this(connection, tableName, OnConflict.NONE);
    }

    public InsertStatement(Connection connection, String tableName, OnConflict onConflict) {
        this.connection = connection;
        this.queryBuilder = new StringBuilder("INSERT ");
        if (onConflict != OnConflict.NONE) {
            queryBuilder
                    .append("OR ")
                    .append(onConflict.name())
                    .append(" ");
        }
        queryBuilder.append("INTO ")
                .append(tableName)
                .append(" VALUES (");
        this.tableName = tableName;
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
        } catch (SQLException e) {
            if (e.getMessage().contains("SQLITE_CONSTRAINT_FOREIGNKEY")) {
                ForeignKeyException ex = getForeignKeyException();
                if (ex != null) throw ex;
            }
            throw e;
        }
    }

    /**
     * Returns a {@link TTSFileException} with appropriate error message for display in the UI
     * by identifying the column and value that caused foreign key constraint violation.
     * @throws SQLException if the queries for determining problematic value causes an SQLException
     */
    private ForeignKeyException getForeignKeyException() throws SQLException {

        HashMap<String, Integer> columnIndices = new HashMap<>();

        ArrayList<String> values = getValueList();

        try (Statement statement = connection.createStatement()) {
            String schema = "";
            String tableName = this.tableName;
            if (this.tableName.contains(".")) {
                schema = tableName.substring(0, tableName.indexOf('.') + 1);
                tableName = this.tableName.substring(this.tableName.indexOf('.') + 1);
            }

            try (ResultSet tableInfoResult = statement.executeQuery("PRAGMA " + schema + "table_info(" + tableName + ")")) {
                while (tableInfoResult.next()) {
                    columnIndices.put(
                            tableInfoResult.getString("name"),
                            tableInfoResult.getInt("cid")
                    );
                }
            }

            try (ResultSet foreignKeyResults = statement
                    .executeQuery("PRAGMA " + schema + "foreign_key_list(" + tableName + ")")) {
                while (foreignKeyResults.next()) {

                    String refTable = foreignKeyResults.getString("table");
                    String refColumn = foreignKeyResults.getString("to");
                    String column = foreignKeyResults.getString("from");
                    String value = values.get(columnIndices.get(column));

                    try (PreparedStatement preparedStatement = connection
                            .prepareStatement("SELECT * FROM " + refTable + " WHERE ? = ? LIMIT 1")) {
                        preparedStatement.setString(1, refColumn);
                        preparedStatement.setString(2, value);
                        try (ResultSet result = preparedStatement.executeQuery()) {
                            if (!result.next()) {
                                return new ForeignKeyException(column);
                            }
                        }
                    }

                }
            }
        }

        return null;
    }

    private ArrayList<String> getValueList() {
        ArrayList<String> values = new ArrayList<>();

        String valuesStr = queryBuilder.toString();
        valuesStr = valuesStr.substring(queryBuilder.indexOf("(") + 1).trim();
        int addedStringPointer = 0;
        for (String value: valuesStr.split(", ")) {
            if (value.equals("?")) {
                value = addedStringValues.get(addedStringPointer++);
            }
            values.add(value);
        }
        return values;
    }

    public static class ForeignKeyException extends RuntimeException {
        private final String columnName;

        private ForeignKeyException(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return this.columnName;
        }
    }

    public enum OnConflict{
        IGNORE,
        REPLACE,
        NONE
    }

}
