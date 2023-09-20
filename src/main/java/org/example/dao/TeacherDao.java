package org.example.dao;

import org.example.pojo.Teacher;

import java.util.HashMap;

public class TeacherDao extends HashMap<String, Teacher> {
    private static TeacherDao instance = null;

    private TeacherDao() {
    }

    public static TeacherDao getInstance() {
        if (instance == null) {
            instance = new TeacherDao();
        }
        return instance;
    }

    @Override
    public Teacher put(String key, Teacher value) {
        return super.put(key.toUpperCase(), value);
    }
}
