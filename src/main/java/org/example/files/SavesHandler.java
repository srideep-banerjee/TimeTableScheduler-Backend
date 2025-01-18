package org.example.files;

import org.example.files.db.ConfigHandler;
import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.entities.Entity;
import org.example.files.db.entities.EntityList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

import static org.example.DefaultConfig.CURRENT_FILE_VERSION;

public class SavesHandler implements AutoCloseable {
    private static SavesHandler instance;
    private Connection connection;
    private boolean saved = true;
    private String currentDbName = null;
    private List<Entity> entities;

    private SavesHandler() {}

    public static SavesHandler getInstance() {
        if (instance == null) {
            instance = new SavesHandler();
            instance.entities = EntityList.getEntityList();
        }
        return instance;
    }

    public void init() throws SQLException, IOException {
        Files.createDirectories(Path.of("sqlite","data"));
        File configFile = Path.of("sqlite","data","tts-config.db")
                .toFile();
        if (!configFile.exists() && !configFile.createNewFile())
            throw new IOException("Couldn't create config database");

        connection = DriverManager.getConnection("jdbc:sqlite:sqlite/data/tts-config.db");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys=1");
            String configCreateQuery = new CreateTableQueryBuilder("config")
                    .addKey("name", "string", true)
                    .addKey("data", "string")
                    .build();
            statement.execute(configCreateQuery);
            loadData(getCurrentSaveName());
        }
    }

    private String getFileNameForSave(String save) {
        return save + "-save.db";
    }

    private String getSaveNameForFileName(String filename) {
        return filename.substring(0, filename.length() - 8);
    }

    public String[] getSavesList() throws SQLException, IOException {
        File savesDir = Path.of(
                "sqlite",
                "data"
        ).toFile();
        if (!savesDir.exists()) return new String[] {getCurrentSaveName()};
        String[] saveFiles = savesDir.list((dir, fileName) -> fileName
                .toLowerCase()
                .endsWith("-save.db"));
        if (saveFiles == null) return new String[] {getCurrentSaveName()};
        for (int i = 0; i < saveFiles.length; i++)
            saveFiles[i] = getSaveNameForFileName(saveFiles[i]).toUpperCase();
        return saveFiles;
    }

    public String getCurrentSaveName() throws SQLException, IOException {
        String currentSave = getConfigHandler().getGlobal("current-save");

        try (
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM config WHERE name='current-save'")
        ) {
            if (result.next()) {
                currentSave = result.getString("data");
            }
        }

        File savesDir = Path.of(
                "sqlite",
                "data"
        ).toFile();
        File currentSaveFile = Path.of(
                "sqlite",
                "data",
                getFileNameForSave(currentSave)
        ).toFile();
        if (!currentSaveFile.exists()) {
            currentSave = null;
        }
        if (currentSave == null) {
            for (String save : Objects.requireNonNullElse(savesDir.list(), new String[0])) {
                if (save.endsWith("-save.db")) {
                    currentSave = getSaveNameForFileName(save);
                    break;
                }
            }
            if (currentSave == null) {
                createNewSave("UNTITLED");
                currentSave = "UNTITLED";
            } else
                updateCurrentSave(currentSave);
        }
        setCurrentDatabase(currentSave, Path.of("sqlite", "data", getFileNameForSave(currentSave)).toString());
        return currentSave;
    }

    private void setCurrentDatabase(String name, String filePath) throws SQLException {
        String currentDbNameBackup = currentDbName;
        try (Statement statement = connection.createStatement()) {
            if (currentDbName != null && !currentDbName.equalsIgnoreCase(name)) {
                statement.execute("DETACH current");
                currentDbName = null;
            }
        }
        if (name == null || name.equalsIgnoreCase(currentDbNameBackup)) return;
        try (PreparedStatement preparedStatement = connection.prepareStatement("ATTACH ? as current")) {
            preparedStatement.setString(1, filePath);
            preparedStatement.execute();
            currentDbName = name;
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (Entity entity: entities) {
                entity.createIfNotExist(statement);
            }
        }
    }

    public void createNewSave(String name) throws SQLException, IOException {
        name = name.toUpperCase();
        String fileName = getFileNameForSave(name);
        File saveFile = Path.of(
                "sqlite",
                "data",
                fileName
        ).toFile();
        if (saveFile.exists()) {
            throw new TTSFileException("New save file already exits.");
        }
        if (!saveFile.createNewFile()) {
            throw new TTSFileException("Couldn't create new save file.");
        }

        try {
            setCurrentDatabase(name, saveFile.getPath());
            connection.setAutoCommit(false);
            createTablesIfNotExist();
            ConfigHandler configHandler = getConfigHandler();
            configHandler.putLocal("version", Integer.toString(CURRENT_FILE_VERSION));
            updateCurrentSave(name);
            connection.commit();
        } catch (Exception e) {
            try (Statement statement = connection.createStatement()) {
                if (currentDbName.equalsIgnoreCase(name))
                    statement.execute("DETACH current");
            } catch (Exception e2) {
                System.err.println("Failed to Detach database after disconnect");
            }
            saveFile.delete();
            throw e;
        }
        connection.setAutoCommit(true);
        saved = true;
    }

    public void loadData(String name) throws SQLException, IOException {
        connection.setAutoCommit(false);

        name = name.toUpperCase();
        String fileName = getFileNameForSave(name);
        File saveFile = Path.of(
                "sqlite",
                "data",
                fileName
        ).toFile();
        if (!saveFile.exists()) {
            throw new TTSFileException("No save file named "+name+" exits.");
        }

        try (Statement statement = connection.createStatement()) {
            setCurrentDatabase(name, saveFile.getPath());
            clearMemory();

            new VersionRectifier(connection).rectifyCurrentFileVersion();

            for (Entity entity: entities) {
                entity.loadFromCurrent(statement);
            }

            updateCurrentSave(name);
            connection.commit();
        } catch (Exception e) {
            if (!connection.getAutoCommit()) connection.rollback();
            e.printStackTrace();
            throw e;
        }
        connection.setAutoCommit(true);
        saved = true;
    }

    public void saveData(String name) throws SQLException, IOException {
        connection.setAutoCommit(false);

        name = name.toUpperCase();
        String fileName = getFileNameForSave(name);
        File saveFile = Path.of(
                "sqlite",
                "data",
                fileName
        ).toFile();
        boolean newFile = !saveFile.exists();
        if (newFile && !saveFile.createNewFile()) {
            throw new TTSFileException("Save file doesn't exist and couldn't create new save file.");
        }

        try {
            setCurrentDatabase(name, saveFile.getPath());
            createTablesIfNotExist();
            clearCurrentDatabase();

            for (Entity entity: entities) {
                entity.saveToCurrent(connection);
            }

            ConfigHandler configHandler = getConfigHandler();
            configHandler.putLocal("version", Integer.toString(CURRENT_FILE_VERSION));

            updateCurrentSave(name);
            connection.commit();
        } catch (Exception e) {
            if (!connection.getAutoCommit()) connection.rollback();
            if (newFile) {
                setCurrentDatabase(null, null);
                saveFile.delete();
            }
            throw e;
        }
        connection.setAutoCommit(true);
        saved = true;
    }

    public void deleteData(String name) throws SQLException {
        if (name.equalsIgnoreCase(currentDbName)) setCurrentDatabase(null, null);
        String fileName = getFileNameForSave(name);
        File saveFile = Path.of(
                "sqlite",
                "data",
                fileName
        ).toFile();
        if (!saveFile.exists()) {
            throw new TTSFileException("No save file named "+name+" exits.");
        }
        if (!saveFile.delete()) {
            throw new TTSFileException("Couldn't delete file");
        }
    }

    public void markUnsaved() {
        saved = false;
    }

    public boolean isSaved() {
        return saved;
    }

    private void clearMemory() {
        for (Entity entity: entities) {
            entity.clearMemory();
        }
    }

    private void clearCurrentDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ListIterator<Entity> li = entities.listIterator(entities.size());
            while (li.hasPrevious()) {
                li.previous().clearDataFromCurrent(statement);
            }
        }
    }

    private void updateCurrentSave(String newSave) throws SQLException {
        newSave = newSave.toUpperCase();
        getConfigHandler().putGlobal("current-save", newSave);
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    public ConfigHandler getConfigHandler() {
        return new ConfigHandler(connection);
    }
}
