package org.example.files.db.entities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Represents a single entity that is stored in the database.
 * A single entity can include multiple tables
 */
public interface Entity {
    /**
     * Used to create the data structure such as tables / initialize with default values
     * for this entity if they don't exist.
     *
     * @param statement the Statement that is used to perform operations
     */
    void createIfNotExist(Statement statement) throws SQLException;

    /**
     * Used to clear the internal memory of the application
     * related to this entity before a load
     */
    void clearMemory();

    /**
     * Clears data related to this entity from the current save file
     *
     * @param statement the Statement that is used to perform operations
     */
    void clearDataFromCurrent(Statement statement) throws SQLException;

    /**
     * Loads data related to this entity from save file to memory
     *
     * @param statement the statement that is used to perform operations
     */
    void loadFromCurrent(Statement statement) throws SQLException, IOException;

    /**
     * Saves data related to this entity to save file from memory
     *
     * @param connection the connection that is used to perform operations
     */
    void saveToCurrent(Connection connection) throws SQLException, IOException;
}
