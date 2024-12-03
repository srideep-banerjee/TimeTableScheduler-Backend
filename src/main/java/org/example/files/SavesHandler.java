package org.example.files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.algorithms.DayPeriod;
import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.files.db.CreateTableQueryBuilder;
import org.example.files.db.InsertStatement;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;
import org.example.pojo.Teacher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import static org.example.files.db.CreateTableQueryBuilder.*;

public class SavesHandler implements AutoCloseable {
    private static SavesHandler instance;
    private Connection connection;
    private boolean saved = true;
    private String currentDbName = null;

    private SavesHandler() {}

    public static SavesHandler getInstance() {
        if (instance == null) {
            instance = new SavesHandler();
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
        String currentSave = null;

        try (
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT * FROM config WHERE name='current-save'");
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
            ForeignKey subjectCodeForeignKey = new ForeignKey("subjects")
                    .addReference("subject_code");
            ForeignKey teacherNameForeignKey = new ForeignKey("teachers")
                    .addReference("teacher_name");
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
            query = new CreateTableQueryBuilder("current.teachers")
                    .addKey("teacher_name", "string", true)
                    .addKey("always_free", "integer")
                    .build();
            statement.execute(query);
            query = new CreateTableQueryBuilder("current.teacher_known_subjects")
                    .addKey("teacher_name", "string")
                    .addKey("subject_code", "string")
                    .primaryKeys("teacher_name", "subject_code")
                    .addForeignKey(teacherNameForeignKey)
                    .addForeignKey(subjectCodeForeignKey)
                    .build();
            statement.execute(query);
            query = new CreateTableQueryBuilder("current.teacher_free")
                    .addKey("teacher_name", "string")
                    .addKey("day_period", "integer")
                    .primaryKeys("teacher_name", "day_period")
                    .addForeignKey(teacherNameForeignKey)
                    .build();
            statement.execute(query);
            query = new CreateTableQueryBuilder("current.schedule_subject_details")
                    .addKey("sem", "integer")
                    .addKey("sec", "integer")
                    .addKey("subject_code", "string")
                    .addKey(new KeyEntry("teacher_name", "string").notNull(false))
                    .addKey("room_code", "string")
                    .primaryKeys("sem", "sec", "subject_code")
                    .addForeignKey(subjectCodeForeignKey)
                    .addForeignKey(teacherNameForeignKey)
                    .addForeignKey(
                            new ForeignKey("subject_room_codes")
                                    .addReference("room_code")
                                    .addReference("subject_code")
                    )
                    .build();
            statement.execute(query);
            query = new CreateTableQueryBuilder("current.schedule_room_entries")
                    .addKey("sem", "integer")
                    .addKey("sec", "integer")
                    .addKey("subject_code", "string")
                    .addKey("room_code", "string")
                    .primaryKeys("sem", "sec", "subject_code")
                    .addForeignKey(subjectCodeForeignKey)
                    .addForeignKey(
                            new ForeignKey("subject_room_codes")
                                    .addReference("room_code")
                                    .addReference("subject_code")
                    ).build();
            statement.execute(query);
            query = new CreateTableQueryBuilder("current.schedule_period_entries")
                    .addKey("sem", "integer")
                    .addKey("sec", "integer")
                    .addKey("day_period", "integer")
                    .addKey("subject_code", "string")
                    .addKey(new KeyEntry("teacher_name", "string").notNull(false))
                    .primaryKeys("sem", "sec", "day_period")
                    .addForeignKey(teacherNameForeignKey)
                    .addForeignKey(
                            new ForeignKey("schedule_room_entries")
                                    .addReference("sem")
                                    .addReference("sec")
                                    .addReference("subject_code")
                    ).build();
            statement.execute(query);
            query = new CreateTableQueryBuilder("current.schedule_structure")
                    .addKey("data", "string")
                    .build();
            statement.execute(query);
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
            String json = new ObjectMapper().writeValueAsString(ScheduleStructure.getRevertedClone());
            new InsertStatement(connection, "current.schedule_structure")
                    .add(json)
                    .executeInsert();
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
            try (ResultSet results = statement.executeQuery("SELECT data FROM current.schedule_structure")) {
                while (results.next()) {
                    String structureJson = results.getString(1);
                    ObjectMapper om = new ObjectMapper();
                    om.readerForUpdating(ScheduleStructure.getInstance())
                            .readValue(structureJson, ScheduleStructure.class);
                }
            }
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
            try (ResultSet results = statement.executeQuery("SELECT * FROM current.teachers")) {
                while (results.next()) {
                    String teacherName = results.getString("teacher_name");
                    boolean alwaysFree = results.getInt("always_free") == 1;
                    HashSet<List<Integer>> freeTime = new HashSet<>();
                    if (!alwaysFree) {
                        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT day_period FROM current.teacher_free WHERE teacher_name = ?")) {
                            preparedStatement.setString(1, teacherName);
                            try (ResultSet teacherArrayResults = preparedStatement.executeQuery()) {
                                while (teacherArrayResults.next()) {
                                    DayPeriod dayPeriod = new DayPeriod(teacherArrayResults.getShort("day_period"));
                                    freeTime.add(Arrays.asList((int) dayPeriod.day, (int) dayPeriod.period));
                                }
                            }
                        }
                    }
                    HashSet<String> knownSubjects = new HashSet<>();
                    try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT subject_code FROM current.teacher_known_subjects WHERE teacher_name = ?")) {
                        preparedStatement.setString(1, teacherName);
                        try (ResultSet teacherArrayResults = preparedStatement.executeQuery()) {
                            while (teacherArrayResults.next())
                                knownSubjects.add(teacherArrayResults.getString("subject_code"));
                        }
                    }
                    TeacherDao.getInstance().put(teacherName, new Teacher(freeTime, knownSubjects));
                }
            }
            ScheduleSolution scheduleSolution = ScheduleSolution.getInstance();
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM current.schedule_room_entries NATURAL JOIN current.schedule_period_entries")) {
                scheduleSolution.updateStructure();
                ScheduleSolution.SolutionAccumulator accumulator = new ScheduleSolution.SolutionAccumulator();
                while (resultSet.next()) {
                    int sem = resultSet.getInt("sem");
                    int sec = resultSet.getInt("sec");
                    DayPeriod dayPeriod = new DayPeriod(resultSet.getShort("day_period"));
                    String subject = resultSet.getString("subject_code");
                    String teacher = resultSet.getString("teacher_name");
                    String room = resultSet.getString("room_code");
                    accumulator.add(sem, sec, dayPeriod.day, dayPeriod.period, subject, teacher, room);
                }
                scheduleSolution.setData(accumulator.accumulate());
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

            for (String teacherName: TeacherDao.getInstance().keySet()) {
                Teacher teacher = TeacherDao.getInstance().get(teacherName);
                new InsertStatement(connection, "current.teachers")
                        .add(teacherName)
                        .add(teacher.getFreeTime().isEmpty())
                        .executeInsert();
                for (String subjectCode: teacher.getSubjects()) {
                    try {
                        new InsertStatement(connection, "current.teacher_known_subjects")
                                .add(teacherName)
                                .add(subjectCode)
                                .executeInsert();
                    } catch (InsertStatement.ForeignKeyException e) {
                        if (e.getColumnName().equals("subject_code")) {
                            String msg = "'" +
                                    subjectCode +
                                    "' was added as known subject for '" +
                                    teacherName +
                                    "'" +
                                    " but doesn't exist in subjects";
                            throw new TTSFileException(msg);
                        }
                    }
                }
                for (List<Integer> freeDayPeriod: teacher.getFreeTime()) {
                    byte day = freeDayPeriod.get(0).byteValue();
                    byte period = freeDayPeriod.get(1).byteValue();
                    new InsertStatement(connection, "current.teacher_free")
                            .add(teacherName)
                            .add(new DayPeriod(day, period).getCompact())
                            .executeInsert();
                }
            }

            ScheduleSolution.SolutionIteratorCallback callback = (sem, sec, day, period, subject, teacher, room) -> {
                try {
                    new InsertStatement(connection, "current.schedule_room_entries", true)
                            .add(sem)
                            .add(sec)
                            .add(subject)
                            .add(room)
                            .executeInsert();
                    new InsertStatement(connection, "current.schedule_period_entries")
                            .add(sem)
                            .add(sec)
                            .add(new DayPeriod((byte) day, (byte) period).getCompact())
                            .add(subject)
                            .add(teacher)
                            .executeInsert();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };
            try {
                new ScheduleSolution.SolutionIterator(callback).iterate();
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw new IOException(e);
            }

            String json = new ObjectMapper().writeValueAsString(ScheduleStructure.getInstance());
            new InsertStatement(connection, "current.schedule_structure")
                    .add(json)
                    .executeInsert();

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

    public void deleteData(String name) throws IOException, SQLException {
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
        TeacherDao.getInstance().clear();
        SubjectDao.getInstance().clear();
        ScheduleStructure.getInstance().setSemesterCount((byte) 0);
        ScheduleStructure.getInstance().setPeriodCount((byte) 0);
        ScheduleStructure.getInstance().setSectionsPerSemester(new byte[0]);
        ScheduleStructure.getInstance().setBreaksPerSemester(new byte[0][0]);
        ScheduleSolution.getInstance().updateStructure();
        ScheduleSolution.getInstance().resetData();
    }

    private void clearCurrentDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM current.subjects");
            statement.execute("DELETE FROM current.subject_room_codes");
            statement.execute("DELETE FROM current.teachers");
            statement.execute("DELETE FROM current.teacher_known_subjects");
            statement.execute("DELETE FROM current.teacher_free");
            statement.execute("DELETE FROM current.schedule_room_entries");
            statement.execute("DELETE FROM current.schedule_period_entries");
            statement.execute("DELETE FROM current.schedule_structure");
        }
    }

    private void updateCurrentSave(String newSave) throws SQLException {
        newSave = newSave.toUpperCase();
        try (PreparedStatement preparedStatement = connection
                .prepareStatement("INSERT OR REPLACE INTO config VALUES ('current-save', ?)")) {
            preparedStatement.setString(1, newSave);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    public static String newEmptySave(String name) {
        if (name.equals("null")) return "Can't use name '" + name + "'";
        if (!new File("Saves").exists()) {
            boolean dirCreated = new File("Saves").mkdir();
            if (!dirCreated) return "Couldn't create 'Saves' directory";
        }
        ObjectMapper om = new ObjectMapper();
        File saveFile = new File("Saves" + File.separator + name + ".dat");
        try {
            if (saveFile.exists()) return "File already exists";
            if (!saveFile.createNewFile()) return "Couldn't create file";
            try (PrintStream ps = new PrintStream(saveFile)) {
                ps.println(om.writeValueAsString(new HashMap<>()));
                ps.println(om.writeValueAsString(new HashMap<>()));
                ps.println("{\"semesterCount\":4,\"sectionsPerSemester\":[0,0,1,0],\"periodCount\":9,\"breaksPerSemester\":[[4,5],[5],[5],[5]]}");
                ps.println("true");
            } catch (IOException e) {
                saveFile.delete();
            }
        } catch (IOException e) {
            return "An error occurred while writing new file";
        }
        if (!updateCurrentSaveName(name)) return "Can't update file currently loaded name";
        return null;
    }

    public static String save(String name) {
        if (name.equals("null")) return "Can't use name '" + name + "'";
        if (!new File("Saves").exists()) {
            boolean dirCreated = new File("Saves").mkdir();
            if (!dirCreated) return "Couldn't create 'Saves' directory";
        }
        ObjectMapper om = new ObjectMapper();
        File saveFile = new File("Saves" + File.separator + name + ".dat");
        try {
            if (!saveFile.exists() && !saveFile.createNewFile()) return "Couldn't create file";
            try (PrintStream ps = new PrintStream(saveFile)) {
                ps.println(om.writeValueAsString(SubjectDao.getInstance()));
                ps.println(om.writeValueAsString(TeacherDao.getInstance()));
                ps.println(om.writeValueAsString(ScheduleStructure.getInstance()));
                ps.println(ScheduleSolution.getInstance().isEmpty());
                if (!ScheduleSolution.getInstance().isEmpty())
                    ps.println(om.writeValueAsString(ScheduleSolution.getInstance().getData()));
            } catch (IOException e) {
                saveFile.delete();
            }
        } catch (IOException e) {
            return "An error occurred while writing saved file";
        }
        if (!updateCurrentSaveName(name)) return "Can't update file currently loaded name";
        return null;
    }

    public static String load(String name) {
        if (!new File("Saves").exists()) return "No save file named '" + name + "'";
        ObjectMapper om = new ObjectMapper();
        File saveFile = new File("Saves" + File.separator + name + ".dat");
        if (!saveFile.exists() || name.equals("null"))
            return "No save file named '" + name + "'";
        try (Scanner sc = new Scanner(saveFile)) {
            String data = sc.nextLine();

            //Update SubjectDao
            SubjectDao.getInstance().clear();
            JsonNode arr = om.readTree(data);
            for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
                String code = it.next();
                JsonNode subJson = arr.get(code);
                Subject subject = om.reader().readValue(subJson, Subject.class);
                SubjectDao.getInstance().put(code, subject);
            }
            data = sc.nextLine();

            //Update TeacherDao
            TeacherDao.getInstance().clear();
            arr = om.readTree(data);
            for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
                String tname = it.next();
                JsonNode subJson = arr.get(tname);
                Teacher teacher = om.reader().readValue(subJson, Teacher.class);
                TeacherDao.getInstance().put(tname, teacher);
            }

            //Update ScheduleStructure
            data = sc.nextLine();
            om.readerForUpdating(ScheduleStructure.getInstance()).readValue(data, ScheduleStructure.class);

            //Updating ScheduleSolution
            data = sc.nextLine();
            if (Boolean.parseBoolean(data)) {
                ScheduleSolution.getInstance().setEmpty(Boolean.parseBoolean(data));
            } else {
                List<List<List<List<List<String>>>>> l = new ArrayList<>();
                ScheduleSolution.getInstance().setData(om.reader().readValue(sc.nextLine(), l.getClass()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred while reading saved file";
        }
        if (!updateCurrentSaveName(name)) return "Can't update file currently loaded name";
        return null;
    }

    public static String getCurrentSave() {
        File currentSaveName = new File("Saves" + File.separator + "Currently saved.txt");

        if (!currentSaveName.exists()) {
            return rectifyCurrentSaved();
        }
        try (Scanner sc = new Scanner(currentSaveName)) {
            //if the current save doesn't exist, then rectify the current save file
            String saveName = sc.nextLine();
            if (Arrays.stream(getSaveList()).noneMatch(s -> s.equalsIgnoreCase(saveName)))
                return rectifyCurrentSaved();
            return saveName;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

//    public static boolean isSaved() {
//        String name = getCurrentSave();
//        ObjectMapper om = new ObjectMapper();
//        File saveFile = new File("Saves" + File.separator + name + ".dat");
//        if (!saveFile.exists() || name.equals("null"))
//            return false;
//        try (Scanner sc = new Scanner(saveFile)) {
//            //Comparing subjects
//            String data = sc.nextLine();
//            HashMap<String, Subject> subjects = new HashMap<>();
//            JsonNode arr = om.readTree(data);
//            for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
//                String code = it.next();
//                JsonNode subJson = arr.get(code);
//                Subject subject = om.reader().readValue(subJson, Subject.class);
//                subjects.put(code, subject);
//            }
//            if (!subjects.equals(SubjectDao.getInstance())) {
//                return false;
//            }
//
//            //Comparing Teachers
//            data = sc.nextLine();
//            HashMap<String, Teacher> teachers = new HashMap<>();
//            arr = om.readTree(data);
//            for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
//                String tname = it.next();
//                JsonNode subJson = arr.get(tname);
//                Teacher teacher = om.reader().readValue(subJson, Teacher.class);
//                teachers.put(tname, teacher);
//            }
//            if (!teachers.equals(TeacherDao.getInstance()))
//                return false;
//
//            //Comparing ScheduleStructure
//            data = sc.nextLine();
//            ScheduleStructure scheduleStructure = ScheduleStructure.getInstance();
//            byte[] sectionsPerSemester = scheduleStructure.getSectionsPerSemester();
//            byte semesterCount = scheduleStructure.getSemesterCount();
//            byte periodCount = scheduleStructure.getPeriodCount();
//            byte[][] breaksPerSemester = scheduleStructure.getBreaksPerSemester();
//            om.readerForUpdating(scheduleStructure).readValue(data, ScheduleStructure.class);
//            if (!Arrays.equals(sectionsPerSemester, scheduleStructure.getSectionsPerSemester()))
//                return false;
//            if (semesterCount != scheduleStructure.getSemesterCount())
//                return false;
//            if (periodCount != scheduleStructure.getPeriodCount())
//                return false;
//            if (!Arrays.deepEquals(breaksPerSemester, scheduleStructure.getBreaksPerSemester()))
//                return false;
//            scheduleStructure.setSemesterCount(semesterCount);
//            scheduleStructure.setSectionsPerSemester(sectionsPerSemester);
//            scheduleStructure.setPeriodCount(periodCount);
//            scheduleStructure.setBreaksPerSemester(breaksPerSemester);
//
//            //Comparing ScheduleSolution
//            data = sc.nextLine();
//            if (Boolean.parseBoolean(data) != ScheduleSolution.getInstance().isEmpty())
//                return false;
//            if (!Boolean.parseBoolean(data)) {
//                List<List<List<List<List<String>>>>> l = new ArrayList<>();
//                l = om.reader().readValue(sc.nextLine(), l.getClass());
//                if (!l.equals(ScheduleSolution.getInstance().getData()))
//                    return false;
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

    private static String rectifyCurrentSaved() {
        System.out.println("Rectifying Current saved");
        String[] saveList = getSaveList();

        if (saveList.length == 0) {
            newEmptySave("UNTITLED");
            return "UNTITLED";
        }
        else {
            updateCurrentSaveName(saveList[0]);
            return saveList[0];
        }
    }

    private static boolean updateCurrentSaveName(String name) {
        File currentSaveName = new File("Saves" + File.separator + "Currently saved.txt");
        try {
            if (!currentSaveName.exists() && !currentSaveName.createNewFile()) return false;
            try (PrintStream ps = new PrintStream(currentSaveName)) {
                ps.println(name);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static String[] getSaveList() {
        File savesDir = new File("Saves");
        if (!savesDir.exists()) return new String[0];
        String[] res = savesDir.list((dir, name) -> !name.equals("Currently saved.txt"));
        for (int i = 0; i < res.length; i++)
            res[i] = res[i].substring(0, res[i].length() - 4);
        return res;
    }

    public static String delete(String name) {
        if (!new File("Saves").exists()) return "No save file named '" + name + "'";
        File saveFile = new File("Saves" + File.separator + name + ".dat");
        if (!saveFile.exists() || name.equals("null"))
            return "No save file named '" + name + "'";
        if (!saveFile.delete()) return "Couldn't delete file";
        return null;
    }
}
