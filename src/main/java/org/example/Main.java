package org.example;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.files.SavesHandler;
import org.example.network.ApiActionHelper;
import org.example.network.LocalServer;
import org.example.ui.JcefLauncher;

import java.awt.*;
import java.net.URI;

public class Main {
    static LocalServer ls;

    public static void main(String[] args) {
        ls = new LocalServer();

        ApiActionHelper aah = ApiActionHelper.getInstance();

        aah.setAction("shutdown", () -> {
            ls.stop();
            System.exit(0);
        });

        aah.setAction("reset", () -> {
            SubjectDao.getInstance().clear();
            TeacherDao.getInstance().clear();
        });

        String saveName = SavesHandler.getCurrentSave();
        if (saveName != null) SavesHandler.load(saveName);

        JcefLauncher jcefLauncher = new JcefLauncher(ls.getDefaultURL());
        jcefLauncher.launch();
    }
}