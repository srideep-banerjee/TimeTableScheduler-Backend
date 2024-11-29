package org.example;

import org.example.files.SavesHandler;
import org.example.network.LocalServer;
import org.example.network.TokenManager;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    static LocalServer ls;

    public static void main(String[] args) {
        System.setProperty("org.sqlite.tmpdir","sqlite");
        try(SavesHandler savesHandler = SavesHandler.getInstance()) {
            savesHandler.init();
            ls = new LocalServer();

            TokenManager.generateNewRandomToken();

            try {
                ProcessBuilder processBuilder =new ProcessBuilder("ShortJRE\\bin\\javaw", "-jar", "TTSBrowserComponent.jar", ls.getDefaultURL(), TokenManager.token);
                Process p= processBuilder.start();
                p.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            ls.stop();
            System.exit(0);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}