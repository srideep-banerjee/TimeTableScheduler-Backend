package org.example.files.db.entities.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.files.db.ConfigHandler;
import org.example.files.db.entities.Entity;
import org.example.pojo.ScheduleStructure;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ScheduleStructureEntity implements Entity {
    @Override
    public void createIfNotExist(Statement statement) throws SQLException {
        ConfigHandler configHandler = new ConfigHandler(statement.getConnection());
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(ScheduleStructure.getRevertedClone());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        configHandler.putLocal("schedule-structure", json);
    }

    @Override
    public void clearMemory() {
        ObjectMapper om = new ObjectMapper();
        try {
            String json = om.writeValueAsString(ScheduleStructure.getRevertedClone());
            om.readerForUpdating(ScheduleStructure.getInstance())
                    .readValue(json, ScheduleStructure.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearDataFromCurrent(Statement statement) throws SQLException {
        ConfigHandler configHandler = new ConfigHandler(statement.getConnection());
        configHandler.deleteLocal("schedule-structure");
    }

    @Override
    public void loadFromCurrent(Statement statement) throws SQLException, IOException {
        ConfigHandler configHandler = new ConfigHandler(statement.getConnection());
        String structureJson = configHandler.getLocal("schedule-structure");
        ObjectMapper om = new ObjectMapper();
        om.readerForUpdating(ScheduleStructure.getInstance())
                .readValue(structureJson, ScheduleStructure.class);
    }

    @Override
    public void saveToCurrent(Connection connection) throws SQLException, IOException {
        ConfigHandler configHandler = new ConfigHandler(connection);
        String json = new ObjectMapper().writeValueAsString(ScheduleStructure.getInstance());
        configHandler.putLocal("schedule-structure", json);
    }
}
