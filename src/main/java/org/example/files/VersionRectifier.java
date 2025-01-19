package org.example.files;

import org.example.files.db.ConfigHandler;
import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.entities.implementation.StudentEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VersionRectifier {
    public static int CURRENT_FILE_VERSION = 4;

    private final Connection connection;

    public VersionRectifier(Connection connection) {
        this.connection = connection;
    }

    /**
     * Rectifies the {@code current} db file to {@code CURRENT_FILE_VERSION} if necessary
     * @throws SQLException if database access error occurs
     */
    public void rectifyCurrentFileVersion() throws SQLException {
        ConfigHandler configHandler = new ConfigHandler(connection);
        String query = new CreateTableQueryBuilder("current.config")
                .addKey("name", "string", true)
                .addKey("data", "string")
                .build();
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        }
        String versionString = configHandler.getLocal("version");
        int version = 0;
        if (versionString != null) version = Integer.parseInt(versionString);

        if (version == CURRENT_FILE_VERSION) return;

        if (version < 1) {
            try (
                    Statement statement = connection.createStatement();
                    ResultSet results = statement.executeQuery("SELECT data FROM current.schedule_structure")
            ) {
                results.next();
                String json = results.getString(1);
                configHandler.putLocal("schedule-structure", json);
                statement.execute("DROP TABLE current.schedule_structure");
            }
        }

        if (version < 2) {
            try (Statement statement = connection.createStatement()) {
                new StudentEntity().createIfNotExist(statement);
            }
        }

        if (version < 3) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE current.students");
                new StudentEntity().createIfNotExist(statement);
            }
        }

        if (version < 4) {
            try (Statement statement = connection.createStatement()) {
                query = "ALTER TABLE current.students ADD COLUMN phone_no STRING";
                statement.execute(query);
                query = "ALTER TABLE current.students ADD COLUMN address STRING";
                statement.execute(query);
            }
        }

        configHandler.putLocal("version", Integer.toString(CURRENT_FILE_VERSION));
    }
}
