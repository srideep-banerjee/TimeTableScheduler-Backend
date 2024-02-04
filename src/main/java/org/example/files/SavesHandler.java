package org.example.files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;
import org.example.pojo.Teacher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class SavesHandler {

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

    private static String rectifyCurrentSaved() {
        System.out.println("Rectifying Current saved");
        String[] saveList = getSaveList();

        if (saveList.length == 0) {
            save("UNTITLED");
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
