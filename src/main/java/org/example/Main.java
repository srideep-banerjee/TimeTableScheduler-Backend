package org.example;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import javax.swing.JFrame;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.files.SavesHandler;
import org.example.network.ApiActionHelper;
import org.example.network.LocalServer;

import java.awt.*;
import java.io.File;
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

        /*try {
            Desktop.getDesktop().browse(new URI(ls.getDefaultURL()));
        } catch (Exception e) {
            System.out.println(e);
        }*/

        //Create a new CefAppBuilder instance
        CefAppBuilder builder = new CefAppBuilder();

        //Check if jcef is installed, if so skip installation
        File jcefInstallDir=new File("jcef-bundle");
        if(jcefInstallDir.exists())builder.setSkipInstallation(true);

        //Configure the builder instance
        builder.getCefSettings().windowless_rendering_enabled = false;

        builder.setAppHandler(new MavenCefAppHandlerAdapter(){});

        //Build a CefApp instance using the configuration above
        CefApp app=null;
        try {
            app = builder.build();
        } catch (IOException | InterruptedException | CefInitializationException | UnsupportedPlatformException e) {
            System.out.println(e);
            System.exit(1);
        }

        // Create a CefClient instance
        CefClient client = app.createClient();

        // Create a CefBrowser instance
        CefBrowser browser = client.createBrowser(ls.getDefaultURL(), false, false);

        // Create a JFrame to host the browser
        JFrame frame = new JFrame("Time Table Scheduler");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}