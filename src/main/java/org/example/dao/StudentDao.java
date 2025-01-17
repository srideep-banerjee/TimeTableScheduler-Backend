package org.example.dao;

import org.example.pojo.Student;

import java.util.HashMap;

public class StudentDao  extends HashMap<String, Student> {
    private static StudentDao instance = null;

    private StudentDao() {
    }

    public static StudentDao getInstance() {
        if (instance == null) {
            instance = new StudentDao();
        }
        return instance;
    }

    @Override
    public Student put(String key, Student value) {
        return super.put(key.toUpperCase(), value);
    }
}
