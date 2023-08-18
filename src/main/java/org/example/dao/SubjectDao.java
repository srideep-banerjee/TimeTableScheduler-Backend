package org.example.dao;

import org.example.pojo.Subject;

import java.util.HashMap;

public class SubjectDao extends HashMap<String, Subject> {
    private static SubjectDao instance = null;

    private SubjectDao() {
    }

    public static SubjectDao getInstance() {
        if (instance == null) {
            instance = new SubjectDao();
        }
        return instance;
    }
}
