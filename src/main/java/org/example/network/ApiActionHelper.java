package org.example.network;

import org.example.interfaces.ApiAction;

import java.util.HashMap;

public class ApiActionHelper {
    private static ApiActionHelper instance;
    private HashMap<String, ApiAction> actions;

    private ApiActionHelper() {
    }

    public static ApiActionHelper getInstance() {
        return instance;
    }

    public void setAction(String name, ApiAction action) {
        if (action != null) actions.put(name, action);
    }

    public boolean performAction(String name) {
        if (!actions.containsKey(name)) return false;
        actions.get(name).perform();
        return true;
    }

    public void clear() {
        actions.clear();
    }
}
