package org.example;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.files.SavesHandler;
import org.example.network.ApiActionHelper;
import org.example.network.LocalServer;

import java.io.IOException;

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
        //new ChromosomeAnalyzerTest().test();

        //ChromosomeTest.startTest();

        try {
            ProcessBuilder processBuilder =new ProcessBuilder("java", "-jar", "TTSBrowserComponent.jar", ls.getDefaultURL());
            Process p= processBuilder.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Process exited");
        ls.stop();
        System.exit(0);
    }
}