package org.example;

import org.example.files.SavesHandler;
import org.example.network.LocalServer;

import java.io.IOException;

public class Main {
    static LocalServer ls;

    public static void main(String[] args) {
        ls = new LocalServer();

        String saveName = SavesHandler.getCurrentSave();
        if (saveName != null) SavesHandler.load(saveName);
        //new ChromosomeAnalyzerTest().test();

        //ChromosomeTest.startTest();

        try {
            ProcessBuilder processBuilder =new ProcessBuilder("ShortJRE\\bin\\java", "-jar", "TTSBrowserComponent.jar", ls.getDefaultURL());
            Process p= processBuilder.start();var l=p.getErrorStream();
            p.waitFor();
            System.out.println("Process exited with code "+p.exitValue());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        ls.stop();
        System.exit(0);
    }
}