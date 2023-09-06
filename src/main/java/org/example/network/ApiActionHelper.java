package org.example.network;

import org.example.interfaces.ApiAction;

import java.util.HashMap;

public class ApiActionHelper {
    private static ApiActionHelper instance;
    private HashMap<String, ApiAction> actions;

    private ApiActionHelper() {
    }

    public static ApiActionHelper getInstance() {
        if(instance==null){
            instance=new ApiActionHelper();
            instance.actions=new HashMap<>();
        }
        return instance;
    }

    public void setAction(String name, ApiAction action) {
        if (action != null) actions.put(name.toUpperCase(), action);
    }

    public boolean performAction(String name) {
        if (!actions.containsKey(name.toUpperCase())) return false;
        actions.get(name.toUpperCase()).perform();
        return true;
    }
}
