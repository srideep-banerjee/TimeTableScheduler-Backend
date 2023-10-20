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

    public String getTheoryOfPractical(String code) {
        code = code.toUpperCase();
        if (!containsKey(code)) return null;
        String theory = code.replace('9', '0');
        if (!containsKey(theory) || theory.equals(code)) return null;
        return theory;
    }

    public String getPracticalOfTheory(String code) {
        code = code.toUpperCase();
        if (!containsKey(code)) return null;
        String practical = code.replace('0', '9');
        if (!containsKey(practical) || practical.equals(code)) return null;
        return practical;
    }

    @Override
    public Subject put(String key, Subject value) {
        return super.put(key.toUpperCase(), value);
    }
}
