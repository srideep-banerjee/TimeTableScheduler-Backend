package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.files.SavesHandler;
import org.example.network.ApiActionHelper;
import org.example.network.LocalServer;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

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

        /*try {
            Desktop.getDesktop().browse(new URI(ls.getDefaultURL()));
        } catch (Exception e) {
            System.out.println(e);
        }*/
        JavaFXApplication.launchURL(ls.getDefaultURL());
    }
}