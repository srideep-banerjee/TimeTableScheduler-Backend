package org.example.dao;

import java.util.HashMap;

public class ConfigDao extends HashMap<String, String> {

    private static ConfigDao instance = null;

    private ConfigDao() {
    }

    public static ConfigDao getInstance() {
        if (instance == null) {
            instance = new ConfigDao();
        }
        return instance;
    }
}
