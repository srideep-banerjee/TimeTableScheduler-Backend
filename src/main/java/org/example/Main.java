package org.example;

import org.example.files.SavesHandler;
import org.example.network.LocalServer;
import org.example.network.TokenManager;

import java.io.IOException;

public class Main {
    static LocalServer ls;

    public static void main(String[] args) {
        ls = new LocalServer();

        String saveName = SavesHandler.getCurrentSave();
        if (saveName != null) SavesHandler.load(saveName);
        //new ChromosomeAnalyzerTest().test();

        //ChromosomeTest.startTest();

        TokenManager.generateNewRandomToken();

        try {
            ProcessBuilder processBuilder =new ProcessBuilder("Short-Jre\\bin\\javaw", "-jar", "TTSBrowserComponent.jar", ls.getDefaultURL(), TokenManager.token);
            Process p= processBuilder.start();
            p.waitFor();
            ////System.out.println("Process exited with code "+p.exitValue());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        ls.stop();
        System.exit(0);
    }
}