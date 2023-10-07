package org.example;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.files.SavesHandler;
import org.example.network.ApiActionHelper;
import org.example.network.LocalServer;

import java.awt.Desktop;
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

        try {
            Desktop.getDesktop().browse(new URI(ls.getDefaultURL()));
        } catch (Exception e) {
            System.out.println(e);
        }
        while(true);
        //JavaFXApplication.launchURL(ls.getDefaultURL());
    }
}